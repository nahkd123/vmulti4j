package io.github.nahkd123.vmulti4j;

public class ReportWriter {
	public static final int NORMAL_REPORT_ID = 0x05;
	public static final int EXTENDED_REPORT_ID = 0x06;
	public static final byte VMULTI_ID = 0x40;

	/**
	 * <p>
	 * Writer digitizer report data to buffer. The VMulti report ID for this is 0x40
	 * or 64.
	 * </p>
	 * 
	 * @param buf
	 * @param bufOffset
	 * @param isExtended
	 * @param buttons
	 * @param x
	 * @param y
	 * @param pressure
	 * @param tiltX
	 * @param tiltY
	 */
	public static void writeDigitizerReport(byte[] buf, int bufOffset, boolean isExtended, byte buttons, short x, short y, short pressure, byte tiltX, byte tiltY) {
		buf[bufOffset + 0] = 11; // Size of digitizer report data
		buf[bufOffset + 1] = (byte) (isExtended ? EXTENDED_REPORT_ID : NORMAL_REPORT_ID);
		buf[bufOffset + 2] = buttons;
		buf[bufOffset + 3] = (byte) (x & 0xFF);
		buf[bufOffset + 4] = (byte) ((x >> 8) & 0xFF);
		buf[bufOffset + 5] = (byte) (y & 0xFF);
		buf[bufOffset + 6] = (byte) ((y >> 8) & 0xFF);
		buf[bufOffset + 7] = (byte) (pressure & 0xFF);
		buf[bufOffset + 8] = (byte) ((pressure >> 8) & 0xFF);
		buf[bufOffset + 9] = tiltX;
		buf[bufOffset + 10] = tiltY;
	}
}
