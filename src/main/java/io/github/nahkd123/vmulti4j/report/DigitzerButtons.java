package io.github.nahkd123.vmulti4j.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record DigitzerButtons(int flags) {
	public static final byte TIP = 0b00000001;
	public static final byte BARREL = 0b00000010;
	public static final byte ERASER = 0b00000100;
	public static final byte INVERT = 0b00001000;
	public static final byte PRESENT = 0b00010000;

	public DigitzerButtons() {
		this(0);
	}

	public DigitzerButtons(boolean present, boolean tip, boolean barrel, boolean eraser, boolean invert) {
		this((present ? PRESENT : 0)
			| (tip ? TIP : 0)
			| (barrel ? BARREL : 0)
			| (eraser ? ERASER : 0)
			| (invert ? INVERT : 0));
	}

	public boolean tip() {
		return (flags & TIP) != 0;
	}

	public boolean barrel() {
		return (flags & BARREL) != 0;
	}

	public boolean eraser() {
		return (flags & ERASER) != 0;
	}

	public boolean invert() {
		return (flags & INVERT) != 0;
	}

	public boolean present() {
		return (flags & PRESENT) != 0;
	}

	public DigitzerButtons withTip(boolean state) {
		return new DigitzerButtons((flags & ~TIP) | (state ? TIP : 0));
	}

	public DigitzerButtons withBarrel(boolean state) {
		return new DigitzerButtons((flags & ~BARREL) | (state ? BARREL : 0));
	}

	public DigitzerButtons withEraser(boolean state) {
		return new DigitzerButtons((flags & ~ERASER) | (state ? ERASER : 0));
	}

	public DigitzerButtons withInvert(boolean state) {
		return new DigitzerButtons((flags & ~INVERT) | (state ? INVERT : 0));
	}

	public DigitzerButtons withPresent(boolean state) {
		return new DigitzerButtons((flags & ~PRESENT) | (state ? PRESENT : 0));
	}

	@Override
	public final String toString() {
		List<String> states = new ArrayList<>();
		if (present()) states.add("present");
		if (tip()) states.add("tip");
		if (barrel()) states.add("barrel");
		if (eraser()) states.add("eraser");
		if (invert()) states.add("invert");
		return "DigitizerButtons[%s]".formatted(states.stream().collect(Collectors.joining(", ")));
	}
}
