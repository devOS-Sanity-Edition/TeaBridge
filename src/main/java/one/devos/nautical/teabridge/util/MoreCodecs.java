package one.devos.nautical.teabridge.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.net.URI;
import java.util.function.Function;

public class MoreCodecs {
    public static final Codec<URI> URI = Codec.STRING.comapFlatMap(checkedMapper(java.net.URI::create), java.net.URI::toString);

    public static <A, B> Function<A, DataResult<B>> checkedMapper(CheckedMappingFunction<A, B> mapper) {
        return mapper;
    }

    @FunctionalInterface
    public interface CheckedMappingFunction<A, B> extends Function<A, DataResult<B>> {
        B map(A a) throws Exception;

        @Override
        default DataResult<B> apply(A a) {
            try {
                return DataResult.success(map(a));
            } catch (Exception e) {
                return DataResult.error(e::getMessage);
            }
        }
    }

    public static <B> Codec<B> fromString(CheckedMappingFunction<String, B> mapper) {
        return Codec.STRING.comapFlatMap(checkedMapper(mapper), String::valueOf);
    }
}
