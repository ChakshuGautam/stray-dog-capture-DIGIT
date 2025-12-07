# SDCRS Complete Configuration Files - Part 2
## Access Control, UI, Flutter, DSS Dashboards

---

# 5. ACCESS CONTROL CONFIGURATION

## 5.1 File: `data/pb/ACCESSCONTROL-ROLES/roles.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "ACCESSCONTROL-ROLES",
  "roles": [
    {
      "code": "TEACHER",
      "name": "Teacher",
      "description": "Teacher who reports stray dog incidents"
    },
    {
      "code": "VERIFIER",
      "name": "SDCRS Verifier",
      "description": "Verifies incident reports"
    },
    {
      "code": "SDCRS_ADMIN",
      "name": "SDCRS Administrator",
      "description": "District level SDCRS administrator"
    },
    {
      "code": "SDCRS_STATE_ADMIN",
      "name": "SDCRS State Administrator",
      "description": "State level SDCRS administrator"
    },
    {
      "code": "MC_ADMIN",
      "name": "Municipal Corp Admin",
      "description": "Municipal Corporation administrator who assigns officers"
    },
    {
      "code": "MC_OFFICER",
      "name": "Municipal Corp Officer",
      "description": "Field officer for dog capture operations"
    },
    {
      "code": "AUTO_ESCALATE",
      "name": "Auto Escalation System",
      "description": "System role for automated workflow escalations"
    }
  ]
}
```

## 5.2 File: `data/pb/ACCESSCONTROL-ACTIONS-TEST/actions-test.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "ACCESSCONTROL-ACTIONS-TEST",
  "actions-test": [
    {
      "id": 3001,
      "name": "SDCRS Incident Create",
      "url": "/sdcrs-services/v1/incident/_create",
      "displayName": "Create Incident",
      "orderNumber": 1,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3002,
      "name": "SDCRS Incident Update",
      "url": "/sdcrs-services/v1/incident/_update",
      "displayName": "Update Incident",
      "orderNumber": 2,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3003,
      "name": "SDCRS Incident Search",
      "url": "/sdcrs-services/v1/incident/_search",
      "displayName": "Search Incidents",
      "orderNumber": 3,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3004,
      "name": "SDCRS Incident Count",
      "url": "/sdcrs-services/v1/incident/_count",
      "displayName": "Count Incidents",
      "orderNumber": 4,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3005,
      "name": "SDCRS Teacher Dashboard",
      "url": "/sdcrs-services/v1/teacher/_dashboard",
      "displayName": "Teacher Dashboard",
      "orderNumber": 5,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3006,
      "name": "SDCRS Admin Dashboard",
      "url": "/sdcrs-services/v1/admin/_dashboard",
      "displayName": "Admin Dashboard",
      "orderNumber": 6,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3007,
      "name": "SDCRS Verifier Inbox",
      "url": "/sdcrs-services/v1/inbox/_search",
      "displayName": "Verifier Inbox",
      "orderNumber": 7,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3008,
      "name": "SDCRS Teacher Payout History",
      "url": "/sdcrs-services/v1/payout/_search",
      "displayName": "Payout History",
      "orderNumber": 8,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3009,
      "name": "SDCRS Bulk Payout Process",
      "url": "/sdcrs-services/v1/payout/_process",
      "displayName": "Process Payouts",
      "orderNumber": 9,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    },
    {
      "id": 3010,
      "name": "SDCRS MC Action Create",
      "url": "/sdcrs-mc-services/v1/action/_create",
      "displayName": "Create MC Action",
      "orderNumber": 10,
      "enabled": true,
      "serviceCode": "SDCRS_MC",
      "code": "null",
      "path": ""
    },
    {
      "id": 3011,
      "name": "SDCRS MC Action Update",
      "url": "/sdcrs-mc-services/v1/action/_update",
      "displayName": "Update MC Action",
      "orderNumber": 11,
      "enabled": true,
      "serviceCode": "SDCRS_MC",
      "code": "null",
      "path": ""
    },
    {
      "id": 3012,
      "name": "SDCRS MC Action Search",
      "url": "/sdcrs-mc-services/v1/action/_search",
      "displayName": "Search MC Actions",
      "orderNumber": 12,
      "enabled": true,
      "serviceCode": "SDCRS_MC",
      "code": "null",
      "path": ""
    },
    {
      "id": 3013,
      "name": "SDCRS MC Inbox",
      "url": "/sdcrs-mc-services/v1/inbox/_search",
      "displayName": "MC Inbox",
      "orderNumber": 13,
      "enabled": true,
      "serviceCode": "SDCRS_MC",
      "code": "null",
      "path": ""
    },
    {
      "id": 3014,
      "name": "SDCRS Reports",
      "url": "/sdcrs-services/v1/reports/_get",
      "displayName": "SDCRS Reports",
      "orderNumber": 14,
      "enabled": true,
      "serviceCode": "SDCRS",
      "code": "null",
      "path": ""
    }
  ]
}
```

## 5.3 File: `data/pb/ACCESSCONTROL-ROLEACTIONS/roleactions.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "ACCESSCONTROL-ROLEACTIONS",
  "roleactions": [
    {
      "rolecode": "TEACHER",
      "actionid": 3001,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 3003,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 3005,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 3008,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 3002,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 3003,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 3004,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 3007,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3002,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3003,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3004,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3006,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3007,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3009,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3010,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_ADMIN",
      "actionid": 3014,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_STATE_ADMIN",
      "actionid": 3002,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_STATE_ADMIN",
      "actionid": 3003,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_STATE_ADMIN",
      "actionid": 3004,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_STATE_ADMIN",
      "actionid": 3006,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_STATE_ADMIN",
      "actionid": 3009,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "SDCRS_STATE_ADMIN",
      "actionid": 3014,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_ADMIN",
      "actionid": 3010,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_ADMIN",
      "actionid": 3011,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_ADMIN",
      "actionid": 3012,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_ADMIN",
      "actionid": 3013,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 3011,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 3012,
      "actioncode": "",
      "tenantId": "pb"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 3013,
      "actioncode": "",
      "tenantId": "pb"
    }
  ]
}
```

---

# 6. LOCALIZATION MESSAGES

## File: Localization API Call

**API**: `POST /localization/messages/v1/_upsert`

```json
{
  "RequestInfo": {
    "apiId": "Rainmaker",
    "authToken": "{{auth-token}}"
  },
  "tenantId": "pb",
  "messages": [
    {
      "code": "SDCRS_COMMON_MODULE_NAME",
      "message": "Stray Dog Capture Reporting System",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_STATUS_PENDINGFORVERIFICATION",
      "message": "Pending Verification",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_STATUS_PENDINGATADMIN",
      "message": "Pending at Admin",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_STATUS_APPROVED",
      "message": "Approved",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_STATUS_REJECTED",
      "message": "Rejected",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_STATUS_DUPLICATE",
      "message": "Duplicate",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_STATUS_CLOSED",
      "message": "Closed",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_MC_STATUS_PENDINGFORASSIGNMENT",
      "message": "Pending Assignment",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_MC_STATUS_PENDINGATLME",
      "message": "Assigned to Officer",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_MC_STATUS_RESOLVED",
      "message": "Resolved",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_MC_STATUS_UNABLETOLOCATE",
      "message": "Unable to Locate",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_MC_STATUS_CLOSEDNOTFOUND",
      "message": "Closed - Not Found",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_SUBMIT",
      "message": "Submit",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_APPROVE",
      "message": "Approve",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_REJECT",
      "message": "Reject",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_ESCALATE",
      "message": "Escalate",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_REOPEN",
      "message": "Reopen",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_MARK_DUPLICATE",
      "message": "Mark as Duplicate",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_ASSIGN",
      "message": "Assign",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ACTION_RESOLVE",
      "message": "Resolve",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_CONDITION_NORMAL",
      "message": "Normal",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_CONDITION_INJURED",
      "message": "Injured",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_CONDITION_AGGRESSIVE",
      "message": "Aggressive",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_CONDITION_PUPPIES",
      "message": "With Puppies",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_CONDITION_RABIES_SUSPECT",
      "message": "Rabies Suspect - URGENT",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_REJECTION_DUPLICATE",
      "message": "Duplicate Report",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_REJECTION_FAKE_IMAGE",
      "message": "Fake/AI-Generated Image",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_REJECTION_NOT_A_DOG",
      "message": "Image Does Not Show a Dog",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_REJECTION_GPS_SPOOFED",
      "message": "GPS Location Spoofed",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_SMS_SUBMITTED",
      "message": "Your stray dog report {incidentNumber} has been submitted successfully. Track status at {url}",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_SMS_APPROVED",
      "message": "Your report {incidentNumber} has been approved! You earned {points} points (Rs.{amount}).",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_SMS_REJECTED",
      "message": "Your report {incidentNumber} was rejected. Reason: {reason}. You can reopen within 5 days if you disagree.",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_SMS_PAYOUT",
      "message": "Rs.{amount} has been credited to your account for {count} approved SDCRS reports. Reference: {reference}",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_SMS_MC_ASSIGNED",
      "message": "MC Action {actionNumber} has been assigned to you. Location: {location}. Please respond within 48 hours.",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_INCIDENT_NUMBER",
      "message": "Incident Number",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_DOG_PHOTO",
      "message": "Dog Photo",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_SELFIE",
      "message": "Selfie with Dog",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_CONDITION",
      "message": "Dog Condition",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_LOCATION",
      "message": "Location",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_NOTES",
      "message": "Additional Notes",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_POINTS_EARNED",
      "message": "Points Earned",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_LABEL_PAYOUT_AMOUNT",
      "message": "Payout Amount",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ERROR_GPS_REQUIRED",
      "message": "GPS location is required. Please enable location services.",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ERROR_PHOTO_REQUIRED",
      "message": "Dog photo is required",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    },
    {
      "code": "SDCRS_ERROR_SELFIE_REQUIRED",
      "message": "Selfie verification is required",
      "module": "rainmaker-sdcrs",
      "locale": "en_IN"
    }
  ]
}
```

---

# 7. INBOX SERVICE CONFIGURATION

## File: `data/pb/inbox-v2/InboxConfiguration.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "INBOX",
  "InboxQueryConfiguration": [
    {
      "module": "SDCRS",
      "index": "sdcrs-incident",
      "allowedSearchCriteria": [
        {
          "name": "tenantId",
          "path": "Data.tenantId.keyword",
          "isMandatory": true,
          "operator": "EQUAL"
        },
        {
          "name": "status",
          "path": "Data.status.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "district",
          "path": "Data.district.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "block",
          "path": "Data.block.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "locality",
          "path": "Data.locality.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "incidentNumber",
          "path": "Data.incidentNumber.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "teacherId",
          "path": "Data.teacherId.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "fromDate",
          "path": "Data.createdTime",
          "isMandatory": false,
          "operator": "GTE"
        },
        {
          "name": "toDate",
          "path": "Data.createdTime",
          "isMandatory": false,
          "operator": "LTE"
        },
        {
          "name": "conditionTags",
          "path": "Data.conditionTags.keyword",
          "isMandatory": false,
          "operator": "IN"
        }
      ],
      "sortBy": {
        "path": "Data.createdTime",
        "defaultOrder": "DESC"
      },
      "sourceFilterPathList": [
        "Data.id",
        "Data.tenantId",
        "Data.incidentNumber",
        "Data.status",
        "Data.district",
        "Data.block",
        "Data.locality",
        "Data.conditionTags",
        "Data.createdTime",
        "Data.capturedAt"
      ]
    },
    {
      "module": "SDCRS_MC",
      "index": "sdcrs-mc-action",
      "allowedSearchCriteria": [
        {
          "name": "tenantId",
          "path": "Data.tenantId.keyword",
          "isMandatory": true,
          "operator": "EQUAL"
        },
        {
          "name": "status",
          "path": "Data.status.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "assignedOfficer",
          "path": "Data.assignedOfficer.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "actionNumber",
          "path": "Data.actionNumber.keyword",
          "isMandatory": false,
          "operator": "EQUAL"
        },
        {
          "name": "fromDate",
          "path": "Data.createdTime",
          "isMandatory": false,
          "operator": "GTE"
        },
        {
          "name": "toDate",
          "path": "Data.createdTime",
          "isMandatory": false,
          "operator": "LTE"
        }
      ],
      "sortBy": {
        "path": "Data.createdTime",
        "defaultOrder": "DESC"
      },
      "sourceFilterPathList": [
        "Data.id",
        "Data.incidentId",
        "Data.tenantId",
        "Data.actionNumber",
        "Data.status",
        "Data.assignedOfficer",
        "Data.scheduledDate",
        "Data.createdTime"
      ]
    }
  ]
}
```

---

# 8. SECURITY POLICY CONFIGURATION

## File: `data/pb/DataSecurity/SecurityPolicy.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "DataSecurity",
  "SecurityPolicy": [
    {
      "model": "SDCRSIncident",
      "uniqueIdentifier": {
        "name": "id",
        "jsonPath": "id"
      },
      "attributes": [
        {
          "name": "teacherMobileNumber",
          "jsonPath": "teacherMobileNumber",
          "patternId": "002",
          "defaultVisibility": "MASKED"
        },
        {
          "name": "teacherName",
          "jsonPath": "teacherName",
          "patternId": "001",
          "defaultVisibility": "MASKED"
        }
      ],
      "roleBasedDecryptionPolicy": [
        {
          "roles": ["SDCRS_ADMIN", "SDCRS_STATE_ADMIN"],
          "attributeAccessList": [
            {
              "attribute": "teacherMobileNumber",
              "firstLevelVisibility": "MASKED",
              "secondLevelVisibility": "PLAIN"
            },
            {
              "attribute": "teacherName",
              "firstLevelVisibility": "PLAIN",
              "secondLevelVisibility": "PLAIN"
            }
          ]
        },
        {
          "roles": ["VERIFIER"],
          "attributeAccessList": [
            {
              "attribute": "teacherMobileNumber",
              "firstLevelVisibility": "NONE",
              "secondLevelVisibility": "MASKED"
            },
            {
              "attribute": "teacherName",
              "firstLevelVisibility": "MASKED",
              "secondLevelVisibility": "PLAIN"
            }
          ]
        },
        {
          "roles": ["MC_ADMIN", "MC_OFFICER"],
          "attributeAccessList": [
            {
              "attribute": "teacherMobileNumber",
              "firstLevelVisibility": "NONE",
              "secondLevelVisibility": "NONE"
            },
            {
              "attribute": "teacherName",
              "firstLevelVisibility": "NONE",
              "secondLevelVisibility": "NONE"
            }
          ]
        }
      ]
    },
    {
      "model": "SDCRSPayout",
      "uniqueIdentifier": {
        "name": "id",
        "jsonPath": "id"
      },
      "attributes": [
        {
          "name": "bankAccountNumber",
          "jsonPath": "bankAccountNumber",
          "patternId": "003",
          "defaultVisibility": "MASKED"
        },
        {
          "name": "ifscCode",
          "jsonPath": "ifscCode",
          "patternId": "004",
          "defaultVisibility": "MASKED"
        }
      ],
      "roleBasedDecryptionPolicy": [
        {
          "roles": ["SDCRS_STATE_ADMIN"],
          "attributeAccessList": [
            {
              "attribute": "bankAccountNumber",
              "firstLevelVisibility": "MASKED",
              "secondLevelVisibility": "PLAIN"
            },
            {
              "attribute": "ifscCode",
              "firstLevelVisibility": "PLAIN",
              "secondLevelVisibility": "PLAIN"
            }
          ]
        }
      ]
    }
  ]
}
```

---

# 9. ID GENERATION FORMAT

## File: `data/pb/common-masters/IdFormat.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "common-masters",
  "IdFormat": [
    {
      "format": "INC-[fy:yyyy-yy]-[SEQ_SDCRS_INCIDENT]",
      "idname": "sdcrs.incident.number",
      "sequenceName": "SEQ_SDCRS_INCIDENT",
      "tenantId": "pb"
    },
    {
      "format": "MC-[fy:yyyy-yy]-[SEQ_SDCRS_MC_ACTION]",
      "idname": "sdcrs.mc.action.number",
      "sequenceName": "SEQ_SDCRS_MC_ACTION",
      "tenantId": "pb"
    },
    {
      "format": "PAY-[fy:yyyy-yy]-W[ww]-[SEQ_SDCRS_PAYOUT]",
      "idname": "sdcrs.payout.number",
      "sequenceName": "SEQ_SDCRS_PAYOUT",
      "tenantId": "pb"
    }
  ]
}
```

---

# 10. HELM/DEPLOYMENT CONFIGURATION

## Add to environment YAML (e.g., `deploy-as-code/helm/environments/sdcrs-dev.yaml`)

```yaml
# SDCRS Services Configuration
sdcrs-services:
  replicas: 2
  images:
    - egovio/sdcrs-services
  heap: "-Xmx512m -Xms512m"
  memory_limits: "768Mi"
  java-args: -Dspring.profiles.active=monitoring
  
  # Application properties
  sdcrs-duplicate-detection-enabled: "true"
  sdcrs-duplicate-threshold: "0.9"
  sdcrs-duplicate-window-days: "7"
  sdcrs-gps-mismatch-threshold-meters: "500"
  sdcrs-auto-review-enabled: "true"
  sdcrs-fraud-detection-enabled: "true"
  
  # Kafka topics
  kafka-topics-save-incident: "save-sdcrs-incident"
  kafka-topics-update-incident: "update-sdcrs-incident"
  kafka-topics-save-verification: "save-sdcrs-verification"
  kafka-topics-save-payout: "save-sdcrs-payout"
  kafka-topics-notification: "egov.core.notification.sms"

sdcrs-mc-services:
  replicas: 1
  images:
    - egovio/sdcrs-mc-services
  heap: "-Xmx384m -Xms384m"
  memory_limits: "512Mi"
  java-args: -Dspring.profiles.active=monitoring
  
  # Kafka topics
  kafka-topics-save-mc-action: "save-sdcrs-mc-action"
  kafka-topics-update-mc-action: "update-sdcrs-mc-action"

# Add to egov-persister config path
egov-persister:
  persist-yml-path: "...,file:///work-dir/configs/egov-persister/sdcrs-persister.yml"

# Add to egov-indexer config path
egov-indexer:
  egov-indexer-yaml-repo-path: "...,file:///work-dir/configs/egov-indexer/sdcrs-indexer.yml"

# Add service host entries
egov-service-host:
  data:
    sdcrs-services: "http://sdcrs-services.egov:8080/"
    sdcrs-mc-services: "http://sdcrs-mc-services.egov:8080/"
```

---

# 11. UI CONFIGURATION (DIGIT-UI)

## 11.1 Inbox Config for Verifier

## File: `data/pb/commonUiConfig/SDCRSInboxConfig.json`

```json
{
  "tenantId": "pb",
  "moduleName": "commonUiConfig",
  "SDCRSInboxConfig": [
    {
      "label": "SDCRS_INBOX",
      "type": "inbox",
      "apiDetails": {
        "serviceName": "/inbox/v2/_search",
        "requestParam": {},
        "requestBody": {
          "inbox": {
            "moduleSearchCriteria": {}
          }
        },
        "minParametersForSearchForm": 0,
        "masterName": "commonUiConfig",
        "moduleName": "SDCRSInboxConfig",
        "tableFormJsonPath": "requestBody.inbox",
        "filterFormJsonPath": "requestBody.inbox.moduleSearchCriteria",
        "searchFormJsonPath": "requestBody.inbox.moduleSearchCriteria"
      },
      "sections": {
        "search": {
          "uiConfig": {
            "headerStyle": null,
            "primaryLabel": "ES_COMMON_SEARCH",
            "secondaryLabel": "ES_COMMON_CLEAR_SEARCH",
            "minReqFields": 0,
            "showFormInst498tead": true,
            "defaultSection": true,
            "defaultValues": {
              "incidentNumber": "",
              "district": "",
              "fromDate": "",
              "toDate": ""
            },
            "fields": [
              {
                "label": "SDCRS_LABEL_INCIDENT_NUMBER",
                "type": "text",
                "isMandatory": false,
                "disable": false,
                "populators": {
                  "name": "incidentNumber",
                  "style": { "marginBottom": "0px" }
                }
              },
              {
                "label": "SDCRS_LABEL_DISTRICT",
                "type": "dropdown",
                "isMandatory": false,
                "disable": false,
                "populators": {
                  "name": "district",
                  "optionsKey": "name",
                  "mdmsConfig": {
                    "masterName": "District",
                    "moduleName": "tenant",
                    "localePrefix": "TENANT_DISTRICT"
                  }
                }
              },
              {
                "label": "ES_COMMON_FROM_DATE",
                "type": "date",
                "isMandatory": false,
                "disable": false,
                "populators": {
                  "name": "fromDate"
                }
              },
              {
                "label": "ES_COMMON_TO_DATE",
                "type": "date",
                "isMandatory": false,
                "disable": false,
                "populators": {
                  "name": "toDate"
                }
              }
            ]
          },
          "label": "",
          "children": {},
          "show": true
        },
        "searchResult": {
          "label": "",
          "uiConfig": {
            "columns": [
              {
                "label": "SDCRS_LABEL_INCIDENT_NUMBER",
                "jsonPath": "incidentNumber",
                "additionalCustomization": true
              },
              {
                "label": "SDCRS_LABEL_DISTRICT",
                "jsonPath": "district"
              },
              {
                "label": "SDCRS_LABEL_BLOCK",
                "jsonPath": "block"
              },
              {
                "label": "SDCRS_LABEL_CONDITION",
                "jsonPath": "conditionTags",
                "additionalCustomization": true
              },
              {
                "label": "ES_COMMON_STATUS",
                "jsonPath": "status",
                "additionalCustomization": true
              },
              {
                "label": "ES_COMMON_CREATED_DATE",
                "jsonPath": "createdTime",
                "additionalCustomization": true
              }
            ],
            "enableGlobalSearch": false,
            "enableColumnSort": true,
            "resultsJsonPath": "items"
          },
          "children": {},
          "show": true
        },
        "filter": {
          "uiConfig": {
            "type": "filter",
            "headerStyle": null,
            "primaryLabel": "ES_COMMON_APPLY",
            "secondaryLabel": "ES_COMMON_CLEAR_FILTER",
            "minReqFields": 0,
            "defaultValues": {
              "status": []
            },
            "fields": [
              {
                "label": "ES_COMMON_STATUS",
                "type": "dropdown",
                "isMandatory": false,
                "disable": false,
                "populators": {
                  "name": "status",
                  "optionsKey": "i18nKey",
                  "options": [
                    { "code": "PENDINGFORVERIFICATION", "i18nKey": "SDCRS_STATUS_PENDINGFORVERIFICATION" },
                    { "code": "PENDINGATADMIN", "i18nKey": "SDCRS_STATUS_PENDINGATADMIN" },
                    { "code": "APPROVED", "i18nKey": "SDCRS_STATUS_APPROVED" },
                    { "code": "REJECTED", "i18nKey": "SDCRS_STATUS_REJECTED" }
                  ],
                  "allowMultiSelect": true
                }
              }
            ]
          },
          "label": "ES_COMMON_FILTER",
          "show": true
        }
      },
      "additionalSections": {}
    }
  ]
}
```

## 11.2 Workflow Popup Config

## File: `data/pb/commonUiConfig/SDCRSWorkflowConfig.json`

```json
{
  "tenantId": "pb",
  "moduleName": "commonUiConfig",
  "SDCRSWorkflowConfig": [
    {
      "businessService": "SDCRS",
      "popupConfig": {
        "APPROVE": {
          "header": "SDCRS_ACTION_APPROVE",
          "fields": [
            {
              "label": "SDCRS_LABEL_COMMENT",
              "type": "textarea",
              "isMandatory": false,
              "populators": {
                "name": "comment"
              }
            }
          ],
          "submitLabel": "SDCRS_ACTION_APPROVE"
        },
        "REJECT": {
          "header": "SDCRS_ACTION_REJECT",
          "fields": [
            {
              "label": "SDCRS_LABEL_REJECTION_REASON",
              "type": "dropdown",
              "isMandatory": true,
              "populators": {
                "name": "rejectionReason",
                "optionsKey": "name",
                "mdmsConfig": {
                  "masterName": "RejectionReasons",
                  "moduleName": "SDCRS",
                  "localePrefix": "SDCRS_REJECTION"
                }
              }
            },
            {
              "label": "SDCRS_LABEL_COMMENT",
              "type": "textarea",
              "isMandatory": true,
              "populators": {
                "name": "comment"
              }
            }
          ],
          "submitLabel": "SDCRS_ACTION_REJECT"
        },
        "MARK_DUPLICATE": {
          "header": "SDCRS_ACTION_MARK_DUPLICATE",
          "fields": [
            {
              "label": "SDCRS_LABEL_DUPLICATE_OF",
              "type": "text",
              "isMandatory": true,
              "populators": {
                "name": "duplicateOf",
                "validation": {
                  "pattern": "^INC-[0-9]{4}-[0-9]{2}-[0-9]+$"
                }
              }
            },
            {
              "label": "SDCRS_LABEL_COMMENT",
              "type": "textarea",
              "isMandatory": false,
              "populators": {
                "name": "comment"
              }
            }
          ],
          "submitLabel": "SDCRS_ACTION_MARK_DUPLICATE"
        },
        "ESCALATE": {
          "header": "SDCRS_ACTION_ESCALATE",
          "fields": [
            {
              "label": "SDCRS_LABEL_ESCALATION_REASON",
              "type": "textarea",
              "isMandatory": true,
              "populators": {
                "name": "comment"
              }
            }
          ],
          "submitLabel": "SDCRS_ACTION_ESCALATE"
        }
      }
    }
  ]
}
```

---

# 12. FLUTTER MOBILE APP CONFIGURATION

## 12.1 Environment Configuration

## File: `frontend/sdcrs_teacher_app/.env`

```env
BASE_URL='https://sdcrs.digit.org/'
MDMS_API_PATH='egov-mdms-service/v1/_search'
GLOBAL_ASSETS='https://sdcrs.digit.org/sdcrs-assets/globalConfig.json'
ENV_NAME="PROD"
```

## 12.2 Global Config

## File: `sdcrs-assets/globalConfig.json`

```json
{
  "tenantId": "pb",
  "stateTenantId": "pb",
  "footerBWLogoURL": "https://sdcrs.digit.org/sdcrs-assets/logo-bw.png",
  "footerLogoURL": "https://sdcrs.digit.org/sdcrs-assets/logo.png",
  "mainLogoURL": "https://sdcrs.digit.org/sdcrs-assets/main-logo.png",
  "enabledModules": [
    "SDCRS"
  ],
  "supportedLanguages": [
    {
      "label": "English",
      "value": "en_IN"
    },
    {
      "label": "हिंदी",
      "value": "hi_IN"
    },
    {
      "label": "ਪੰਜਾਬੀ",
      "value": "pa_IN"
    }
  ],
  "defaultLanguage": "en_IN",
  "gmapsApiKey": "{{GMAPS_API_KEY}}",
  "maxImageSizeKB": 5120,
  "supportedImageFormats": ["jpg", "jpeg", "png"],
  "gpsAccuracyThresholdMeters": 50,
  "offlineSubmissionExpiryHours": 48,
  "helplineNumber": "1800-XXX-XXXX"
}
```

## 12.3 pubspec.yaml Dependencies

```yaml
dependencies:
  flutter:
    sdk: flutter
  digit_components: ^0.0.6
  reactive_forms: ^14.1.0
  provider: ^6.0.5
  flutter_bloc: ^8.1.3
  http: ^0.13.5
  shared_preferences: ^2.2.0
  geolocator: ^10.1.0
  image_picker: ^1.0.4
  camera: ^0.10.5
  connectivity_plus: ^5.0.1
  path_provider: ^2.1.1
  sqflite: ^2.3.0
  intl: ^0.18.1
  flutter_secure_storage: ^9.0.0
  crypto: ^3.0.3
```

---

# 13. DSS DASHBOARD CONFIGURATION

## File: `configs/egov-dss-dashboards/dashboard-analytics/sdcrs-dashboard.json`

```json
{
  "id": "sdcrs-state-dashboard",
  "name": "DSS_SDCRS_DASHBOARD",
  "isActive": true,
  "style": "LINEAR",
  "visualizations": [
    {
      "id": "sdcrsTotalIncidents",
      "name": "DSS_SDCRS_TOTAL_INCIDENTS",
      "dimensions": {
        "height": 350,
        "width": 4
      },
      "vizType": "metric",
      "label": "DSS_SDCRS_TOTAL_INCIDENTS",
      "noUnit": true,
      "isCollapsible": false,
      "charts": [
        {
          "id": "sdcrsTotalIncidentsMetric",
          "name": "DSS_SDCRS_TOTAL_INCIDENTS",
          "code": "",
          "chartType": "metric",
          "filter": "",
          "headers": []
        }
      ]
    },
    {
      "id": "sdcrsApprovalRate",
      "name": "DSS_SDCRS_APPROVAL_RATE",
      "dimensions": {
        "height": 350,
        "width": 4
      },
      "vizType": "metric",
      "label": "DSS_SDCRS_APPROVAL_RATE",
      "noUnit": false,
      "isCollapsible": false,
      "charts": [
        {
          "id": "sdcrsApprovalRateMetric",
          "name": "DSS_SDCRS_APPROVAL_RATE",
          "code": "",
          "chartType": "metric",
          "filter": "",
          "headers": []
        }
      ]
    },
    {
      "id": "sdcrsTotalPayout",
      "name": "DSS_SDCRS_TOTAL_PAYOUT",
      "dimensions": {
        "height": 350,
        "width": 4
      },
      "vizType": "metric",
      "label": "DSS_SDCRS_TOTAL_PAYOUT",
      "noUnit": false,
      "isCollapsible": false,
      "charts": [
        {
          "id": "sdcrsTotalPayoutMetric",
          "name": "DSS_SDCRS_TOTAL_PAYOUT",
          "code": "",
          "chartType": "metric",
          "filter": "",
          "headers": []
        }
      ]
    },
    {
      "id": "sdcrsDistrictWise",
      "name": "DSS_SDCRS_DISTRICT_WISE",
      "dimensions": {
        "height": 450,
        "width": 6
      },
      "vizType": "chart",
      "label": "",
      "noUnit": true,
      "isCollapsible": false,
      "charts": [
        {
          "id": "sdcrsDistrictWiseBar",
          "name": "DSS_SDCRS_DISTRICT_WISE",
          "code": "",
          "chartType": "bar",
          "filter": "",
          "headers": []
        }
      ]
    },
    {
      "id": "sdcrsTrendOverTime",
      "name": "DSS_SDCRS_TREND",
      "dimensions": {
        "height": 450,
        "width": 6
      },
      "vizType": "chart",
      "label": "",
      "noUnit": true,
      "isCollapsible": false,
      "charts": [
        {
          "id": "sdcrsTrendLine",
          "name": "DSS_SDCRS_TREND",
          "code": "",
          "chartType": "line",
          "filter": "",
          "headers": []
        }
      ]
    },
    {
      "id": "sdcrsConditionBreakdown",
      "name": "DSS_SDCRS_CONDITION_BREAKDOWN",
      "dimensions": {
        "height": 350,
        "width": 4
      },
      "vizType": "chart",
      "label": "",
      "noUnit": true,
      "isCollapsible": false,
      "charts": [
        {
          "id": "sdcrsConditionPie",
          "name": "DSS_SDCRS_CONDITION_BREAKDOWN",
          "code": "",
          "chartType": "pie",
          "filter": "",
          "headers": []
        }
      ]
    }
  ]
}
```

---

# 14. PDF SERVICE CONFIGURATION

## File: `configs/pdf-service/data-config/sdcrs-incident-receipt.json`

```json
{
  "key": "sdcrs-incident-receipt",
  "config": {
    "defaultStyle": {
      "font": "Roboto"
    },
    "content": [
      {
        "style": "header",
        "table": {
          "widths": ["*"],
          "body": [
            [
              {
                "text": "STRAY DOG CAPTURE REPORTING SYSTEM",
                "style": "header"
              }
            ],
            [
              {
                "text": "Incident Report Receipt",
                "style": "subheader"
              }
            ]
          ]
        },
        "layout": "noBorders"
      },
      {
        "style": "content",
        "table": {
          "widths": ["30%", "70%"],
          "body": [
            ["Incident Number:", {"text": "{{incidentNumber}}", "bold": true}],
            ["Status:", "{{status}}"],
            ["Submitted On:", "{{submittedDate}}"],
            ["Location:", "{{district}}, {{block}}, {{locality}}"],
            ["Dog Condition:", "{{conditionTags}}"],
            ["Points Awarded:", "{{pointsAwarded}}"],
            ["Payout Amount:", "Rs. {{payoutAmount}}"]
          ]
        },
        "layout": {
          "hLineWidth": function(i, node) { return 0.5; },
          "vLineWidth": function(i, node) { return 0; }
        }
      },
      {
        "text": "\n\nThis is a system generated receipt. No signature required.",
        "style": "footer"
      }
    ],
    "styles": {
      "header": {
        "fontSize": 16,
        "bold": true,
        "alignment": "center",
        "margin": [0, 0, 0, 10]
      },
      "subheader": {
        "fontSize": 12,
        "alignment": "center",
        "margin": [0, 0, 0, 20]
      },
      "content": {
        "fontSize": 10,
        "margin": [0, 5, 0, 5]
      },
      "footer": {
        "fontSize": 8,
        "italics": true,
        "alignment": "center"
      }
    }
  }
}
```

---

# DEPLOYMENT CHECKLIST

## Phase 1: Infrastructure & Core

- [ ] Create PostgreSQL database tables (see data model doc)
- [ ] Create Elasticsearch indices: `sdcrs-incident`, `sdcrs-mc-action`, `sdcrs-payout`
- [ ] Add to `master-config.json`
- [ ] Deploy MDMS files to git repository
- [ ] Restart egov-mdms-service

## Phase 2: Backend Services

- [ ] Create workflow BusinessServices via API
- [ ] Add persister config to egov-persister
- [ ] Add indexer config to egov-indexer
- [ ] Deploy sdcrs-services
- [ ] Deploy sdcrs-mc-services
- [ ] Add service routes to Zuul

## Phase 3: Access Control

- [ ] Add roles to ACCESSCONTROL-ROLES
- [ ] Add actions to ACCESSCONTROL-ACTIONS
- [ ] Add role-action mappings
- [ ] Add Security Policy for PII

## Phase 4: Localization & UI

- [ ] Upsert localization messages
- [ ] Add Inbox configuration to MDMS
- [ ] Add UI configs (workflow popup, inbox search)
- [ ] Deploy DIGIT-UI with SDCRS module

## Phase 5: Mobile App

- [ ] Configure Flutter app environment
- [ ] Deploy global config to assets bucket
- [ ] Build and test APK
- [ ] Publish to Play Store

## Phase 6: Analytics & Reporting

- [ ] Configure DSS dashboard
- [ ] Set up Kibana dashboards
- [ ] Configure PDF templates
- [ ] Set up cron job for auto-escalation

## Phase 7: Go-Live

- [ ] Load test (300K submissions/day)
- [ ] Security audit
- [ ] UAT sign-off
- [ ] Production deployment
- [ ] Monitoring & alerting setup

---

*Complete configuration package for SDCRS on DIGIT Platform*
*Version: 1.0*
*Last Updated: December 2025*
