package one.devos.nautical.teabridge.util;

import java.net.URI;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.dv8tion.jda.api.utils.MiscUtil;

public class MoreCodecs {
	public static final Codec<URI> URI = fromString(java.net.URI::new);
	public static final Codec<Long> SNOWFLAKE = fromString(MiscUtil::parseSnowflake);

	public static <A, B> Function<A, DataResult<B>> checkedMapper(CheckedMappingFunction<A, B> mapper) {
		return mapper;
	}

	@FunctionalInterface
	public interface CheckedMappingFunction<A, B> extends Function<A, DataResult<B>> {
		B map(A a) throws Exception;

		@Override
		default DataResult<B> apply(A a) {
			try {
				return DataResult.success(this.map(a));
			} catch (Exception e) {
				return DataResult.error(e::getMessage);
			}
		}
	}

	public static <B> Codec<B> fromString(CheckedMappingFunction<String, B> mapper) {
		return Codec.STRING.comapFlatMap(checkedMapper(mapper), String::valueOf);
	}
}
