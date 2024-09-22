#!/bin/bash

IP="localhost"

# change order to switch between
FILE_PATH="./01_sample_acquisition.json"
BUNDLE_NAME="01_sample_acquisition"
FILE_PATH="./eval.json"
BUNDLE_NAME="preprocEval"

TEMP_FILE=$(mktemp)
sed "s/PLACEHOLDER/172.17.0.3/g" $FILE_PATH > $TEMP_FILE
openssl dgst -sha256 -sign ./signing_key.pem -out /tmp/sign.sha256 $TEMP_FILE

DOCUMENT=$(base64 -w 0 $TEMP_FILE)
SIGNATURE=$(base64 -w 0 /tmp/sign.sha256)

# create temp file with payload
PAYLOAD_TEMP_FILE=$(mktemp)
cat <<EOF > "$PAYLOAD_TEMP_FILE"
{
    "document": "$DOCUMENT",
    "documentFormat": "json",
    "signature": "$SIGNATURE",
    "clearancePeriod": 30,
    "createdOn": 123
}
EOF

curl --location "http://${IP}:8000/api/v1/organizations/ORG/documents/${BUNDLE_NAME}" \
     --header 'Content-Type: application/json' \
     --data "@${PAYLOAD_TEMP_FILE}"
#     --data "{
#    \"document\": \"$DOCUMENT\",
#    \"documentFormat\": \"json\",
#    \"signature\": \"$SIGNATURE\",
#    \"clearancePeriod\": 30,
#    \"createdOn\": 123
#}"