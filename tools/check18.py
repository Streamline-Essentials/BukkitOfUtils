"""
Verify that compiled BOU classes only call Bukkit APIs that exist in the 1.8.8 API jar.

BOU compiles against Spigot 1.16.5 and `options.release = 11` constrains only java.*,
not org.bukkit.*, so a post-1.8 Bukkit call compiles cleanly and then fails at runtime
with NoSuchMethodError / NoSuchFieldError on a 1.8.8 server.

This parses the constant pool of every 1.8.8 API class (via `javap -p`), builds a full
member table including the inheritance graph, then checks every org.bukkit method/field
reference in our own compiled classes against it.
"""
import re
import subprocess
import sys
import zipfile
from pathlib import Path

API_JAR = sys.argv[1]
ROOTS = sys.argv[2:]

api = zipfile.ZipFile(API_JAR)
api_classes = sorted(n[:-6] for n in api.namelist() if n.endswith(".class"))
present = set(api_classes)

DECL = re.compile(
    r"^(?:public |protected |private )?(?:static )?(?:final |abstract )*"
    r"(?:class|interface|enum)\s+([\w.$]+)(.*)$"
)
MEMBER = re.compile(r"([\w$]+)\s*\(")
FIELD = re.compile(r"([\w$]+);\s*$")

declared = {}   # owner -> set(member names)
parents = {}    # owner -> [supertypes]


def javap_all():
    """Dump every API class once and record declared members plus supertypes."""
    names = [c.replace("/", ".") for c in api_classes]
    for i in range(0, len(names), 80):
        chunk = names[i:i + 80]
        out = subprocess.run(
            ["javap", "-classpath", API_JAR, "-p"] + chunk,
            capture_output=True, text=True,
        ).stdout
        cur = None
        for line in out.splitlines():
            d = DECL.match(line.strip())
            if d:
                cur = d.group(1).replace(".", "/")
                declared.setdefault(cur, set())
                sup = []
                tail = d.group(2)
                # Split the tail into its extends / implements clauses first; a naive
                # search for "extends (.+)" swallows a following "implements ..." and
                # yields a bogus supertype, silently breaking the inheritance walk.
                for kw in ("extends", "implements"):
                    m = re.search(kw + r"\s+(.+?)(?=\s+(?:extends|implements)\s|\s*\{|$)", tail)
                    if m:
                        for t in m.group(1).split(","):
                            t = t.strip().split("<")[0].strip()
                            if t:
                                sup.append(t.replace(".", "/"))
                parents[cur] = sup
                continue
            if cur is None:
                continue
            s = line.strip()
            if not s or s.startswith("Compiled from"):
                continue
            mm = MEMBER.search(s)
            if mm:
                declared[cur].add(mm.group(1))
                continue
            fm = FIELD.search(s)
            if fm:
                declared[cur].add(fm.group(1))


def has_member(owner, member, seen=None):
    """True if owner or any 1.8.8 supertype declares member."""
    if seen is None:
        seen = set()
    if owner in seen:
        return False
    seen.add(owner)
    if member in declared.get(owner, ()):
        return True
    # Everything inherits from Object.
    if member in ("equals", "hashCode", "toString", "getClass", "notify",
                  "notifyAll", "wait", "clone", "finalize"):
        return True
    for p in parents.get(owner, ()):
        if has_member(p, member, seen):
            return True
    # Enums get values()/valueOf()/ordinal()/name()/compareTo() implicitly.
    if member in ("values", "valueOf", "ordinal", "name", "compareTo"):
        return True
    return False


javap_all()

targets = []
for root in ROOTS:
    targets.extend(sorted(Path(root).rglob("*.class")))

if not targets:
    print("no classes found under: " + ", ".join(ROOTS))
    sys.exit(1)

REF = re.compile(r"//\s*(?:Interface)?(?:Method|Field)\s+(org/bukkit/[\w/$]+)\.([\w$<>]+):")

refs = {}
for t in targets:
    out = subprocess.run(["javap", "-c", "-p", str(t)], capture_output=True, text=True).stdout
    for owner, member in REF.findall(out):
        if member in ("<init>", "<clinit>"):
            continue
        refs.setdefault((owner, member), set()).add(t.name)

missing_class, missing_member = [], []
for (owner, member), src in sorted(refs.items()):
    if owner not in present:
        missing_class.append((owner, member, src))
    elif not has_member(owner, member):
        missing_member.append((owner, member, src))

print(f"checked {len(targets)} class files, {len(refs)} distinct Bukkit refs\n")

if missing_class:
    print("=== Bukkit CLASSES absent from 1.8.8 ===")
    for o, m, s in missing_class:
        print(f"  {o}.{m}   <- {', '.join(sorted(s))}")
    print()

if missing_member:
    print("=== Bukkit MEMBERS absent from 1.8.8 ===")
    for o, m, s in missing_member:
        print(f"  {o}.{m}   <- {', '.join(sorted(s))}")
    print()

if not missing_class and not missing_member:
    print("OK: every Bukkit ref resolves against the 1.8.8 API")
else:
    print(f"FAIL: {len(missing_class)} missing classes, {len(missing_member)} missing members")
    sys.exit(2)
