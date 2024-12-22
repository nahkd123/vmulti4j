# vmulti4j
Send digitizer report to VMulti from your favorite Java application!

VMulti is only available for Windows, so of course you have to use Windows.

## Usage
Install [VMulti][vmulti] first, then obtain `VMultiIO` instance like this:

```java
// hid4java
HidServicesSpecification hidServicesSpec = new HidServicesSpecification();
hidServicesSpec.setAutoStart(false);
hidServicesSpec.setAutoDataRead(false);
HidServices hidServices = HidManager.getHidServices(hidServicesSpec);
hidServices.start();

// vmulti4j
try (VMultiIO vmulti = VMultiIO.getIOStream(hidServices.getAttachedHidDevices()).thenOpen()) {
	// interact with vmulti here...
}
```

After that, you can send digitizer report to VMulti:

```java
double positionX = 0.5; // from 0.0 to 1.0; 0.5 for center of X axis
double positionY = 0.5; // like positionX, but for Y axis
double pressure = 0.2; // 20% of max pressure
int tiltX = 0, tiltY = 0; // Tilt angles

vmulti.reportDigitizer(
	new DigitizerButtons().withPresent(true).withTip(true),
	positionX, positionY,
	pressure,
	tiltX,
	tiltY);
```

## License
MIT License.

[vmulti]: https://github.com/X9VoiD/vmulti-bin/releases/latest