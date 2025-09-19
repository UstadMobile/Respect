
```
adb shell cmd -w wifi set-wifi-enabled disabled
adb shell svc data disable
```

Passkey Gotcha's (thank you Google)

* Creation JSON MUST include a timeout, even though it is not required as per the spec.
* RPID cannot be validated: when using *.example.org in AndroidManifest and then using an rpId
  for a subdomain e.g. rpId=school.example.org, then https://school.example.org/.well-known/assetlinks.json 
  itself must return an HTTP 200 OK response (in addition to https://example.org/.well-known/assetlinks.json ). 

