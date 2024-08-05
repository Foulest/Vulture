/*
 * This file is part of Pledge - https://github.com/ThomasOM/Pledge
 * Copyright (C) 2021 Thomazz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.thomazz.pledge.packet;

import com.google.common.collect.ImmutableSet;
import dev.thomazz.pledge.packet.providers.PingPongPacketProvider;
import dev.thomazz.pledge.packet.providers.TransactionPacketProvider;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@UtilityClass
public class PacketProviderFactory {

    private final Set<ThrowingSupplier<PingPacketProvider>> suppliers = ImmutableSet.of(
            TransactionPacketProvider::new,
            PingPongPacketProvider::new
    );

    public PingPacketProvider buildPingProvider() {
        return suppliers.stream()
                .map(PacketProviderFactory::buildProvider)
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not create packet provider!"));
    }

    private Optional<PingPacketProvider> buildProvider(ThrowingSupplier<PingPacketProvider> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {

        T get() throws Exception;
    }
}
