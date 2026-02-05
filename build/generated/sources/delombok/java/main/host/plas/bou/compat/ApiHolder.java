package host.plas.bou.compat;

import gg.drak.thebase.objects.Identifiable;
import java.util.Optional;
import java.util.function.Function;

public abstract class ApiHolder<A> implements Identifiable {

    public enum ApiTristate {
        TRUE, FALSE, NULL;
    }

    private String identifier;
    private Optional<A> api;
    private Function<Void, A> getter;

    public ApiHolder(String identifier, Function<Void, A> getter) {
        this.identifier = identifier;
        this.getter = getter;
        tryEnable();
    }

    public A checkApi() {
        try {
            return getter.apply(null);
        } catch (Throwable t) {
            return null;
        }
    }

    public void tryEnable() {
        A api = checkApi();
        if (api != null) {
            this.api = Optional.of(api);
        } else {
            this.api = Optional.empty();
        }
    }

    public boolean isEnabled() {
        return api.isPresent();
    }

    public A api() {
        return api.orElse(null);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Optional<A> getApi() {
        return this.api;
    }

    public Function<Void, A> getGetter() {
        return this.getter;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setApi(final Optional<A> api) {
        this.api = api;
    }

    public void setGetter(final Function<Void, A> getter) {
        this.getter = getter;
    }
}
