using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using Google.Cloud.Storage.V1;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using RevLogixApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Firebase settings come from configuration / environment variables on the host
// (Firebase__ProjectId, Firebase__CredentialsJson, Firebase__StorageBucket) - see docs/SETUP.md.
var projectId = builder.Configuration["Firebase:ProjectId"];
var credentialsJson = builder.Configuration["Firebase:CredentialsJson"];
if (string.IsNullOrWhiteSpace(projectId) || string.IsNullOrWhiteSpace(credentialsJson))
{
    throw new InvalidOperationException(
        "Firebase:ProjectId and Firebase:CredentialsJson must be set. See docs/SETUP.md.");
}

var credential = GoogleCredential.FromJson(credentialsJson);
FirebaseApp.Create(new AppOptions { Credential = credential, ProjectId = projectId });

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// NoSQL database + blob storage + push notifications, all in the same Firebase project
builder.Services.AddSingleton(new FirestoreDbBuilder
{
    ProjectId = projectId,
    JsonCredentials = credentialsJson
}.Build());
builder.Services.AddSingleton(StorageClient.Create(credential));
builder.Services.AddSingleton<PushService>();
builder.Services.AddSingleton<MaintenanceAlertService>();

// Every request must carry a valid Firebase ID token. The user id inside the token decides
// which Firestore documents the request can see.
builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.Authority = $"https://securetoken.google.com/{projectId}";
        options.MapInboundClaims = false;
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = $"https://securetoken.google.com/{projectId}",
            ValidateAudience = true,
            ValidAudience = projectId,
            ValidateLifetime = true
        };
    });
builder.Services.AddAuthorization();

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
        policy.AllowAnyOrigin().AllowAnyMethod().AllowAnyHeader());
});

var app = builder.Build();

app.UseCors("AllowAll");
app.UseAuthentication();
app.UseAuthorization();

// Simple health check so the host (and a browser) can see the API is up
app.MapGet("/", () => "RevLogix API is running");
app.MapControllers();

app.Run();
