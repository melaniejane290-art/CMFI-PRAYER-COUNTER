"""Fail immediately on Apple post-upload errors; confirm a valid TestFlight build."""
import os
import time

import jwt
import requests

BUNDLE_ID = os.environ["BUNDLE_ID"]
BUILD = os.environ["IOS_BUILD_NUMBER"]
session = requests.Session()


def get(path, params=None):
    now = int(time.time())
    token = jwt.encode(
        {"iss": os.environ["APP_STORE_CONNECT_ISSUER_ID"], "iat": now,
         "exp": now + 600, "aud": "appstoreconnect-v1"},
        os.environ["APP_STORE_CONNECT_PRIVATE_KEY"], algorithm="ES256",
        headers={"kid": os.environ["APP_STORE_CONNECT_KEY_IDENTIFIER"]},
    )
    response = session.get("https://api.appstoreconnect.apple.com/v1/" + path,
                           headers={"Authorization": "Bearer " + token},
                           params=params, timeout=45)
    response.raise_for_status()
    return response.json()["data"]


apps = get("apps", {"filter[bundleId]": BUNDLE_ID, "limit": 1})
if not apps:
    raise SystemExit(f"No App Store Connect app for {BUNDLE_ID}.")
APP_ID = apps[0]["id"]

for attempt in range(40):
    for upload in get(f"apps/{APP_ID}/buildUploads", {"limit": 50}):
        attrs = upload["attributes"]
        if attrs["cfBundleVersion"] != BUILD:
            continue
        state = attrs["state"]
        print(f"Apple upload {upload['id']}: {state['state']}", flush=True)
        if state["state"] == "FAILED":
            for error in state.get("errors", []):
                print(f"::error::Apple {error.get('code')}: {error.get('description')}", flush=True)
            raise SystemExit(1)
    for build in get("builds", {"filter[app]": APP_ID, "filter[version]": BUILD, "limit": 10}):
        state = build["attributes"]["processingState"]
        print(f"TestFlight build {BUILD}: {state}", flush=True)
        if state == "VALID":
            print(f"TestFlight accepted build {BUILD}, id {build['id']}", flush=True)
            raise SystemExit(0)
        if state in {"FAILED", "INVALID"}:
            raise SystemExit(f"Apple processing failed: {state}")
    time.sleep(30)
raise SystemExit("Apple processing is still pending; inspect App Store Connect before uploading again.")
