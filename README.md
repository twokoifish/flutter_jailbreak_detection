# flutter_jailbreak_detection

Flutter jailbreak and root detection plugin.


It uses [RootBeer](https://github.com/scottyab/rootbeer) on Android,
and [IOSSecuritySuite](https://github.com/securing/IOSSecuritySuite) on iOS.

### Notes from TwoKoiFish

Updated with a recompiled version of RootBeer to support 16 KB page sizes on Android 15+ devices.

## Getting Started

```
import 'package:flutter_jailbreak_detection/flutter_jailbreak_detection.dart';

bool jailbroken = await FlutterJailbreakDetection.jailbroken;
bool developerMode = await FlutterJailbreakDetection.developerMode; // android only.

```
