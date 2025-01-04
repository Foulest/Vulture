package net.foulest.packetevents.utils.vector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vector2f implements Externalizable, Cloneable {

    public float x;
    public float y;

    public Vector2f(float d) {
        x = d;
        y = d;
    }

    @Contract(pure = true)
    public Vector2f(float @NotNull [] xy) {
        x = xy[0];
        y = xy[1];
    }

    public Vector2f set(float d) {
        x = d;
        y = d;
        return this;
    }

    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2f set(double d) {
        x = (float) d;
        y = (float) d;
        return this;
    }

    public Vector2f set(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
        return this;
    }

    public Vector2f set(float @NotNull [] xy) {
        x = xy[0];
        y = xy[1];
        return this;
    }

    /**
     * Get a specific component of this vector.
     *
     * @param component The component to retrieve (0 or 1).
     * @return The value of the component.
     * @throws IllegalArgumentException If the component is not 0 or 1.
     */
    public float get(int component) {
        switch (component) {
            case 0:
                return x;
            case 1:
                return y;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Vector2f get(@NotNull Vector2f dest) {
        dest.x = x;
        dest.y = y;
        return dest;
    }

    /**
     * Set a specific component of this vector to a value.
     *
     * @param component The component to set (0 or 1).
     * @param value    The value to set the component to.
     * @return The vector.
     * @throws IllegalArgumentException If the component is not 0 or 1.
     */
    public Vector2f setComponent(int component, float value) {
        switch (component) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return this;
    }

    public Vector2f perpendicular() {
        float temp = y;
        y = x * -1.0F;
        x = temp;
        return this;
    }

    public Vector2f sub(float x, float y) {
        return sub(x, y, this);
    }

    @Contract("_, _, _ -> param3")
    private @NotNull Vector2f sub(float x, float y, @NotNull Vector2f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        return dest;
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public static float lengthSquared(float x, float y) {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float distance(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceSquared(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public Vector2f add(float x, float y) {
        return add(x, y, this);
    }

    public Vector2f add(float x, float y, @NotNull Vector2f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        return dest;
    }

    public Vector2f zero() {
        x = 0.0F;
        y = 0.0F;
        return this;
    }

    public void writeExternal(@NotNull ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    public void readExternal(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
        x = in.readFloat();
        y = in.readFloat();
    }

    public Vector2f negate() {
        return negate(this);
    }

    @Contract("_ -> param1")
    private @NotNull Vector2f negate(@NotNull Vector2f dest) {
        dest.x = -x;
        dest.y = -y;
        return dest;
    }

    public Vector2f mul(float scalar) {
        return mul(scalar, this);
    }

    @Contract("_, _ -> param2")
    private @NotNull Vector2f mul(float scalar, @NotNull Vector2f dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        return dest;
    }

    public Vector2f mul(float x, float y) {
        return mul(x, y, this);
    }

    @Contract("_, _, _ -> param3")
    private @NotNull Vector2f mul(float x, float y, @NotNull Vector2f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        return dest;
    }

    public Vector2f div(float scalar) {
        return div(scalar, this);
    }

    @Contract("_, _ -> param2")
    private @NotNull Vector2f div(float scalar, @NotNull Vector2f dest) {
        float inv = 1.0F / scalar;
        dest.x = x * inv;
        dest.y = y * inv;
        return dest;
    }

    public Vector2f div(float x, float y) {
        return div(x, y, this);
    }

    @Contract("_, _, _ -> param3")
    private @NotNull Vector2f div(float x, float y, @NotNull Vector2f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        return dest;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            Vector2f other = (Vector2f) obj;

            if (Float.floatToIntBits(x) == Float.floatToIntBits(other.x)) {
                return Float.floatToIntBits(y) == Float.floatToIntBits(other.y);
            } else {
                return false;
            }
        }
    }

    public boolean equals(float x, float y) {
        if (Float.floatToIntBits(this.x) == Float.floatToIntBits(x)) {
            return Float.floatToIntBits(this.y) == Float.floatToIntBits(y);
        } else {
            return false;
        }
    }

    public int maxComponent() {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        return absX >= absY ? 0 : 1;
    }

    public int minComponent() {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        return absX < absY ? 0 : 1;
    }

    public Vector2f floor() {
        return floor(this);
    }

    @Contract("_ -> param1")
    private @NotNull Vector2f floor(@NotNull Vector2f dest) {
        dest.x = (float) Math.floor(x);
        dest.y = (float) Math.floor(y);
        return dest;
    }

    public Vector2f ceil() {
        return ceil(this);
    }

    @Contract("_ -> param1")
    private @NotNull Vector2f ceil(@NotNull Vector2f dest) {
        dest.x = (float) Math.ceil(x);
        dest.y = (float) Math.ceil(y);
        return dest;
    }

    public Vector2f round() {
        return round(this);
    }

    @Contract("_ -> param1")
    private @NotNull Vector2f round(@NotNull Vector2f dest) {
        dest.x = Math.round(x);
        dest.y = Math.round(y);
        return dest;
    }

    public Vector2f absolute() {
        return absolute(this);
    }

    @Contract("_ -> param1")
    private @NotNull Vector2f absolute(@NotNull Vector2f dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        return dest;
    }

    public Vector2f clone() throws CloneNotSupportedException {
        return (Vector2f) super.clone();
    }
}
