import pyAesCrypt
import requests
import os
import json
import sys

url = os.getenv("UPLOAD_URL")
token = os.getenv("TOKEN")
commit = os.getenv("VERSION")
branch = os.getenv("BRANCH")
message = os.getenv("MESSAGE")
type_upload = sys.argv[1]

pyAesCrypt.encryptFile("minusbounce.zip", "minusbounce.zip.enc", token)
info = {"type": type_upload, "commit": commit, "branch": branch, "message": message}
data = {"content" : json.dumps(info), "username" : "github artifact",}
resp = requests.post(url, files = {"file1": open("minusbounce.zip.enc", "rb"), 'payload_json': (None, json.dumps(data))})

print(resp.status_code)