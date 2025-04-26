package one.devos.nautical.teabridge.util;

import java.net.URI;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class MoreCodecs {
	public static final Codec<URI> URI = fromString(java.net.URI::new);

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
