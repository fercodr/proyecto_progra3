using EntreFunciones.Components;
using EntreFunciones.Seguridad;
var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();
builder.Services.AddSeguridad();
builder.Services.AddHttpClient<EntreFunciones.Servicios.Rest.CompraRestService>((sp, client) =>
{
    string url=sp.GetRequiredService<IConfiguration>()["Rest:BaseUrl"] ?? "http://localhost:8080/entrefunciones-rest/v1/api/";
    client.BaseAddress=new Uri(url.TrimEnd('/')+"/");
    client.Timeout=TimeSpan.FromSeconds(20);
});

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}
app.UseStatusCodePagesWithReExecute("/not-found", createScopeForStatusCodePages: true);
app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();
app.UseAntiforgery();

app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();
app.MapSesionEndpoints();
app.Run();
