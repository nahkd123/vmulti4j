package io.github.nahkd123.vmulti4j.descriptor;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record Descriptor(List<Item> root) implements ItemQueriable {
	public static Descriptor parse(byte[] data, int offset, int length) {
		ByteArrayInputStream stream = new ByteArrayInputStream(data, offset, length);
		DataInput input = new DataInputStream(stream);
		List<Item> root = new ArrayList<>();

		while (stream.available() > 0) {
			try {
				root.add(Item.parse(input));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return new Descriptor(root);
	}

	@Override
	public Optional<Item> query(Predicate<Item> predicate) {
		return root.stream().filter(predicate).findAny();
	}

	public long getMaxReportBits(boolean isInput) {
		List<Item> application = getApplicationCollection();
		return countBits(isInput, application) + 8;
	}

	public boolean hasReportId(int id) {
		return hasReportId(id, root);
	}

	public List<Item> getApplicationCollection() {
		return query(i -> i.type() == ItemType.MAIN
			&& i.tag() == Item.MAIN_COLLECTION
			&& i.value() == 0x01)
			.map(i -> i.children())
			.orElse(Collections.emptyList());
	}

	private static boolean hasReportId(int id, Collection<Item> items) {
		for (Item item : items) {
			if (item.type() == ItemType.MAIN && item.tag() == Item.MAIN_COLLECTION) {
				if (hasReportId(id, item.children())) return true;
			}

			if (item.type() == ItemType.GLOBAL && item.tag() == Item.GLOBAL_REPORT_ID) {
				if ((item.value() & 0x7FFFFFFF) == id) return true;
			}
		}

		return false;
	}

	private static long countBits(boolean isInput, List<Item> items) {
		long bits = 0;
		int reportSize = 0;
		int reportCount = 0;

		for (Item item : items) {
			if (item.type() == ItemType.MAIN) {
				if (item.tag() == Item.MAIN_COLLECTION) bits += countBits(isInput, item.children());
				if (item.tag() == (isInput ? Item.MAIN_INPUT : Item.MAIN_OUTPUT)) bits += reportSize * reportCount;
				continue;
			}

			if (item.type() == ItemType.GLOBAL) {
				if (item.tag() == Item.GLOBAL_REPORT_SIZE) reportSize = item.value() & 0x7FFFFFFF;
				if (item.tag() == Item.GLOBAL_REPORT_COUNT) reportCount = item.value() & 0x7FFFFFFF;
			}
		}

		return bits;
	}
}
