package io.github.nahkd123.vmulti4j.descriptor;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record Item(ItemType type, int tag, int value, List<Item> children) implements ItemQueriable {

	// Main item tag
	public static final int MAIN_INPUT = 0b1000;
	public static final int MAIN_OUTPUT = 0b1001;
	public static final int MAIN_FEATURE = 0b1011;
	public static final int MAIN_COLLECTION = 0b1010;
	public static final int MAIN_END_COLLECTION = 0b1100;

	// Global item tag
	public static final int GLOBAL_USAGE_PAGE = 0b0000;
	public static final int GLOBAL_LOGICAL_MIN = 0b0001;
	public static final int GLOBAL_LOGICAL_MAX = 0b0010;
	public static final int GLOBAL_PHYSICAL_MIN = 0b0011;
	public static final int GLOBAL_PHYSICAL_MAX = 0b0100;
	public static final int GLOBAL_UNIT_EXPONENT = 0b0101;
	public static final int GLOBAL_UNIT = 0b0110;
	public static final int GLOBAL_REPORT_SIZE = 0b0111;
	public static final int GLOBAL_REPORT_ID = 0b1000;
	public static final int GLOBAL_REPORT_COUNT = 0b1001;
	public static final int GLOBAL_PUSH = 0b1010;
	public static final int GLOBAL_POP = 0b1011;

	// Local item tag
	public static final int LOCAL_USAGE = 0b0000;
	public static final int LOCAL_USAGE_MIN = 0b0001;
	public static final int LOCAL_USAGE_MAX = 0b0010;
	public static final int LOCAL_DESIGNATOR = 0b0011;
	public static final int LOCAL_DESIGNATOR_MIN = 0b0100;
	public static final int LOCAL_DESIGNATOR_MAX = 0b0101;
	public static final int LOCAL_STRING = 0b0111;
	public static final int LOCAL_STRING_MIN = 0b1000;
	public static final int LOCAL_STRING_MAX = 0b1001;
	public static final int LOCAL_DELIMITER = 0b1010;

	@Override
	public Optional<Item> query(Predicate<Item> predicate) {
		if (children == null) return Optional.empty();
		return children.stream().filter(predicate).findAny();
	}

	public static Item parse(DataInput stream) throws IOException {
		int header = stream.readUnsignedByte();
		header &= 0xff;
		int tag = header >> 4;
		ItemType type = switch ((header >> 2) & 0b11) {
		case 0b00 -> ItemType.MAIN;
		case 0b01 -> ItemType.GLOBAL;
		case 0b10 -> ItemType.LOCAL;
		default -> ItemType.RESERVED;
		};
		int bytesCount = switch (header & 0b11) {
		case 1 -> 1;
		case 2 -> 2;
		case 3 -> 4;
		default -> 0;
		};
		byte[] bytes = new byte[bytesCount];
		stream.readFully(bytes);
		int value = 0;
		for (int i = 0; i < bytesCount; i++) value |= (bytes[i] & 0xff) << (i * 8);

		// Unsigned to signed
		value = switch (bytesCount) {
		case 1 -> (byte) value;
		case 2 -> (short) value;
		default -> value;
		};

		List<Item> children;
		if (type == ItemType.MAIN && tag == MAIN_COLLECTION) {
			// Collection
			children = new ArrayList<>();
			Item lastChild;

			while ((lastChild = parse(stream)).type() != ItemType.MAIN || lastChild.tag() != MAIN_END_COLLECTION) {
				children.add(lastChild);
			}
		} else {
			children = null;
		}

		return new Item(type, tag, value, children);
	}

	@Override
	public final String toString() {
		switch (type) {
		case MAIN:
			return switch (tag) {
			case MAIN_INPUT -> "Input";
			case MAIN_OUTPUT -> "Output";
			case MAIN_FEATURE -> "Feature";
			case MAIN_COLLECTION -> "Collection (%s) %s".formatted(switch (value) {
			case 0x00 -> "Physical";
			case 0x01 -> "Application";
			case 0x02 -> "Logical";
			case 0x03 -> "Report";
			case 0x04 -> "Named Array";
			case 0x05 -> "Usage Switch";
			case 0x06 -> "Usage Modifier";
			default -> "0x%02x".formatted(value);
			}, children);
			default -> "%s/0x%x: %d".formatted(type, tag, value);
			};
		case GLOBAL:
			return "%s: %d".formatted(switch (tag) {
			case GLOBAL_USAGE_PAGE -> "Usage Page";
			case GLOBAL_LOGICAL_MIN -> "Logical Min";
			case GLOBAL_LOGICAL_MAX -> "Logical Max";
			case GLOBAL_PHYSICAL_MIN -> "Physical Min";
			case GLOBAL_PHYSICAL_MAX -> "Physical Max";
			case GLOBAL_UNIT_EXPONENT -> "Unit Exponent";
			case GLOBAL_UNIT -> "Unit";
			case GLOBAL_REPORT_SIZE -> "Report Size";
			case GLOBAL_REPORT_ID -> "Report ID";
			case GLOBAL_REPORT_COUNT -> "Report Count";
			case GLOBAL_PUSH -> "Push";
			case GLOBAL_POP -> "Pop";
			default -> "%s/0x%x".formatted(type, tag);
			}, value);
		case LOCAL:
			return "%s: %d".formatted(switch (tag) {
			case LOCAL_USAGE -> "Usage";
			case LOCAL_USAGE_MIN -> "Usage Min";
			case LOCAL_USAGE_MAX -> "Usage Max";
			default -> "%s/0x%x".formatted(type, tag);
			}, value);
		default:
			return "%s/0x%x: %d".formatted(type, tag, value);
		}
	}
}
