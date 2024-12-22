package io.github.nahkd123.vmulti4j;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hid4java.HidDevice;

import io.github.nahkd123.vmulti4j.descriptor.Descriptor;
import io.github.nahkd123.vmulti4j.report.DigitzerButtons;

public class VMultiIO implements AutoCloseable {
	private HidDevice io;
	private boolean normal;
	private boolean extended;

	public VMultiIO(HidDevice io, boolean normal, boolean extended) {
		this.io = io;
		this.normal = normal;
		this.extended = extended;
	}

	public static VMultiIO getIOStream(Collection<HidDevice> devices) {
		Map<HidDevice, Descriptor> devToDescriptor = devices.stream()
			.filter(dev -> dev.getVendorId() == 255 && dev.getProductId() == 47820)
			.collect(Collectors.toMap(Function.identity(), dev -> {
				dev.open();
				byte[] data = new byte[4096];
				int dataBytes = dev.getReportDescriptor(data);
				dev.close();
				return Descriptor.parse(data, 0, dataBytes);
			}));

		HidDevice ioDevice = null;
		boolean normal = false, extended = false;

		// Scan for IO stream
		for (Entry<HidDevice, Descriptor> entry : devToDescriptor.entrySet()) {
			Descriptor descriptor = entry.getValue();

			if (descriptor.getMaxReportBits(true) == 520 && descriptor.getMaxReportBits(false) == 520) {
				ioDevice = entry.getKey();
				break;
			}
		}

		// Partially installed / Not installed
		if (ioDevice == null) return null;

		// Scan for output device
		for (Entry<HidDevice, Descriptor> entry : devToDescriptor.entrySet()) {
			Descriptor descriptor = entry.getValue();

			if (descriptor.getMaxReportBits(true) == 80) {
				if (descriptor.hasReportId(ReportWriter.NORMAL_REPORT_ID)) normal = true;
				if (descriptor.hasReportId(ReportWriter.EXTENDED_REPORT_ID)) extended = true;
				if (normal && extended) break;
			}
		}

		// Partially installed
		if (!normal && !extended) return null;
		return new VMultiIO(ioDevice, normal, extended);
	}

	public HidDevice getIO() { return io; }

	public boolean isNormal() { return normal; }

	public boolean isExtended() { return extended; }

	public boolean open() {
		return io.open();
	}

	public VMultiIO thenOpen() {
		open();
		return this;
	}

	public int writeDigitizerReport(byte buttons, short x, short y, short pressure, byte tiltX, byte tiltY) {
		if (!isExtended()) pressure = (short) (pressure / 2);
		byte[] data = new byte[11];
		ReportWriter.writeDigitizerReport(data, 0, extended, buttons, x, y, pressure, tiltX, tiltY);
		return io.write(data, data.length, ReportWriter.VMULTI_ID);
	}

	public int writeDigitizerReport(int buttons, int x, int y, int pressure, int tiltX, int tiltY) {
		return writeDigitizerReport((byte) buttons, (short) x, (short) y, (short) pressure, (byte) tiltX, (byte) tiltY);
	}

	public void reportDigitizer(DigitzerButtons buttons, double x, double y, double pressure, int tiltX, int tiltY) {
		writeDigitizerReport(
			(byte) buttons.flags(),
			(short) (Math.max(x, 0d) * 32767d),
			(short) (Math.max(y, 0d) * 32767d),
			(short) (Math.max(pressure, 0d) * 16535d),
			(byte) tiltX, (byte) tiltY);
	}

	@Override
	public void close() {
		io.close();
	}
}
