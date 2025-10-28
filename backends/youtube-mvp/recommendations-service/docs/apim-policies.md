# API Management Policies

This document contains sample Azure API Management (APIM) policies for the Recommendations Service.

## JWT Validation Policy

### Inbound Policy

```xml
<inbound>
    <!-- Validate JWT token from Entra ID -->
    <validate-jwt header-name="Authorization" 
                  failed-validation-httpcode="401" 
                  failed-validation-error-message="Invalid or expired token">
        
        <openid-config url="https://login.microsoftonline.com/{tenantId}/v2.0/.well-known/openid-configuration" />
        
        <required-claims>
            <claim name="aud" match="all">
                <value>{clientId}</value>
            </claim>
        </required-claims>
    </validate-jwt>
    
    <!-- Extract user ID from token -->
    <set-variable name="userId" value="@(context.Request.Headers.GetValueOrDefault("Authorization", "").AsJwt()?.Subject)" />
    
    <!-- Forward userId as header -->
    <set-header name="X-User-Id" exists-action="override">
        <value>@(context.Variables["userId"])</value>
    </set-header>
    
    <base />
</inbound>
```

## Rate Limiting Policy

### Per-User Rate Limit

```xml
<inbound>
    <rate-limit-by-key 
        calls="100" 
        renewal-period="60" 
        counter-key="@(context.Request.Headers.GetValueOrDefault("Authorization", "").AsJwt()?.Subject ?? "anonymous")" />
    
    <base />
</inbound>
```

### Response for Rate Limit Exceeded

```xml
<on-error>
    <choose>
        <when condition="@(context.LastError.Source == "rate-limit-by-key")">
            <return-response>
                <set-status code="429" reason-phrase="Too Many Requests" />
                <set-body>
                    {
                        "error": {
                            "code": "TooManyRequests",
                            "message": "Rate limit exceeded. Please try again later.",
                            "details": [
                                {
                                    "code": "RateLimitExceeded",
                                    "message": "You have exceeded the allowed request rate of 100 requests per minute."
                                }
                            ]
                        }
                    }
                </set-body>
            </return-response>
        </when>
    </choose>
    <base />
</on-error>
```

## Circuit Breaker Policy

```xml
<inbound>
    <set-backend-service base-url="https://recommendations-service.example.com" />
    
    <forward-request timeout="5" />
    
    <choose>
        <when condition="@(context.Response.StatusCode >= 500)">
            <cache-lookup-value key="@("circuit-breaker-" + context.Request.Url.Path)" />
            <choose>
                <when condition="@(!context.Variables.ContainsKey("failure-count"))">
                    <cache-store-value key="@("circuit-breaker-" + context.Request.Url.Path)" value="1" duration="60" />
                </when>
                <otherwise>
                    <cache-store-value key="@("circuit-breaker-" + context.Request.Url.Path)" 
                                      value="@(int.Parse(context.Variables.GetValueOrDefault("failure-count", "0")) + 1)" 
                                      duration="60" />
                    
                    <choose>
                        <when condition="@(int.Parse(context.Variables.GetValueOrDefault("failure-count", "0")) >= 5)">
                            <return-response>
                                <set-status code="503" />
                                <set-body>Circuit breaker open. Service temporarily unavailable.</set-body>
                            </return-response>
                        </when>
                    </choose>
                </otherwise>
            </choose>
        </when>
    </choose>
    
    <base />
</inbound>
```

## Request/Response Transformation

### Request Transformation (Add Metadata)

```xml
<inbound>
    <set-variable name="request-id" value="@(Guid.NewGuid().ToString())" />
    
    <set-header name="X-Request-ID" exists-action="override">
        <value>@(context.Variables["request-id"])</value>
    </set-header>
    
    <set-header name="X-API-Version" exists-action="override">
        <value>v1</value>
    </set-header>
    
    <base />
</inbound>
```

### Response Transformation (Add Cache Headers)

```xml
<outbound>
    <choose>
        <when condition="@(context.Operation.Name == "GetHomeRecommendations")">
            <set-header name="Cache-Control" exists-action="override">
                <value>public, max-age=300</value>
            </set-header>
        </when>
        <when condition="@(context.Operation.Name == "GetNextUpRecommendations")">
            <set-header name="Cache-Control" exists-action="override">
                <value>public, max-age=120</value>
            </set-header>
        </when>
    </choose>
    
    <base />
</outbound>
```

## CORS Policy

```xml
<inbound>
    <cors allow-credentials="true">
        <allowed-origins>
            <origin>https://example.com</origin>
            <origin>https://www.example.com</origin>
        </allowed-origins>
        <allowed-methods>
            <method>GET</method>
            <method>OPTIONS</method>
        </allowed-methods>
        <allowed-headers>
            <header>Content-Type</header>
            <header>Authorization</header>
            <header>X-Request-ID</header>
        </allowed-headers>
        <expose-headers>
            <header>X-Request-ID</header>
            <header>X-Rate-Limit-Remaining</header>
        </expose-headers>
    </cors>
    
    <base />
</inbound>
```

## Logging and Monitoring

### Log Request Metrics

```xml
<inbound>
    <trace>
        <trace source="inbound" 
               severity="information">
            <message>
                @{
                    return $"Request: {context.Request.Method} {context.Request.Url.Path}, " +
                           $"User: {context.Request.Headers.GetValueOrDefault("Authorization", "").AsJwt()?.Subject}, " +
                           $"Query: {context.Request.Url.QueryString}";
                }
            </message>
        </trace>
    </trace>
    
    <base />
</inbound>
```

### Log Response Metrics

```xml
<outbound>
    <trace>
        <trace source="outbound" 
               severity="information">
            <message>
                @{
                    return $"Response: {context.Response.StatusCode}, " +
                           $"Duration: {context.Elapsed}, " +
                           $"Size: {context.Response.Body.As<string>(preserveContent: true).Length}";
                }
            </message>
        </trace>
    </trace>
    
    <base />
</outbound>
```

## IP Filtering

```xml
<inbound>
    <choose>
        <when condition="@(context.Request.IpAddress != "10.0.0.0/8" && context.Request.IpAddress != "172.16.0.0/12")">
            <return-response>
                <set-status code="403" />
                <set-body>Access denied from this IP address.</set-body>
            </return-response>
        </when>
    </choose>
    
    <base />
</inbound>
```

## Complete Policy Example

```xml
<policies>
    <inbound>
        <!-- CORS -->
        <cors allow-credentials="true">
            <allowed-origins>
                <origin>*</origin>
            </allowed-origins>
        </cors>
        
        <!-- JWT Validation -->
        <validate-jwt header-name="Authorization" failed-validation-httpcode="401">
            <openid-config url="https://login.microsoftonline.com/{tenantId}/v2.0/.well-known/openid-configuration" />
        </validate-jwt>
        
        <!-- Rate Limiting -->
        <rate-limit-by-key 
            calls="100" 
            renewal-period="60" 
            counter-key="@(context.Request.Headers.GetValueOrDefault("Authorization", "").AsJwt()?.Subject)" />
        
        <!-- Set Headers -->
        <set-header name="X-Request-ID" exists-action="override">
            <value>@(Guid.NewGuid().ToString())</value>
        </set-header>
        
        <base />
    </inbound>
    
    <backend>
        <forward-request />
    </backend>
    
    <outbound>
        <!-- Add Cache Headers -->
        <set-header name="Cache-Control" exists-action="override">
            <value>max-age=300</value>
        </set-header>
        
        <set-header name="X-API-Version" exists-action="override">
            <value>v1</value>
        </set-header>
        
        <base />
    </outbound>
    
    <on-error>
        <choose>
            <when condition="@(context.LastError.Source == "rate-limit-by-key")">
                <return-response>
                    <set-status code="429" />
                    <set-body>Rate limit exceeded</set-body>
                </return-response>
            </when>
            <when condition="@(context.Response.StatusCode >= 500)">
                <return-response>
                    <set-status code="503" />
                    <set-body>Service temporarily unavailable</set-body>
                </return-response>
            </when>
        </choose>
        <base />
    </on-error>
</policies>
```

## Usage

### Apply Policy to API

```bash
az apim api policy create \
  --resource-group myResourceGroup \
  --service-name myAPIM \
  --api-id recommendations \
  --policy-format xml \
  --policy-string @policies/recommendations-policy.xml
```

### Apply Policy to Operation

```bash
az apim operation policy set \
  --resource-group myResourceGroup \
  --service-name myAPIM \
  --api-id recommendations \
  --operation-id getHomeRecommendations \
  --policy-format xml \
  --policy-string @policies/get-home-policy.xml
```

## Testing

### Test JWT Validation

```bash
# Valid Token
curl -X GET "https://api.example.com/api/v1/recs/home?userId=123&limit=20" \
  -H "Authorization: Bearer $VALID_JWT_TOKEN"

# Invalid Token
curl -X GET "https://api.example.com/api/v1/recs/home?userId=123&limit=20" \
  -H "Authorization: Bearer invalid_token"
# Expected: 401 Unauthorized
```

### Test Rate Limiting

```bash
# Rapid requests
for i in {1..150}; do
  curl -X GET "https://api.example.com/api/v1/recs/home?userId=123&limit=20" \
    -H "Authorization: Bearer $TOKEN"
done
# After 100 requests: 429 Too Many Requests
```

