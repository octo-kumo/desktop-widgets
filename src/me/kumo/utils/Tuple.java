package me.kumo.utils;

import java.util.Objects;

public final class Tuple<A, B, C> {
    private final A a;
    private final B b;
    private final C c;

    public Tuple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A a() {
        return a;
    }

    public B b() {
        return b;
    }

    public C c() {
        return c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Tuple<?, ?, ?> that = (Tuple<?, ?, ?>) obj;
        return Objects.equals(this.a, that.a) &&
                Objects.equals(this.b, that.b) &&
                Objects.equals(this.c, that.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }

    @Override
    public String toString() {
        return "Tuple[" +
                "a=" + a + ", " +
                "b=" + b + ", " +
                "c=" + c + ']';
    }

}
