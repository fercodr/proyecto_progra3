using EntreFunciones.Components;
using EntreFunciones.Seguridad;
using EntreFunciones.Servicios;
using EntreFunciones.Servicios.Rest;

WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

// Registrar servicios.
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

builder.Services.AddSeguridad();

builder.Services.AddScoped<ICompraService, CompraService>();
builder.Services.AddScoped<IAuditoriaService, AuditoriaService>();
builder.Services.AddScoped<IFuncionService, FuncionService>();
builder.Services.AddScoped<IAdministradorService, AdministradorService>();
builder.Services.AddScoped<ICatalogoService, CatalogoService>();
builder.Services.AddScoped<ISedeService, SedeService>();
builder.Services.AddScoped<ISalaService, SalaService>();
builder.Services.AddScoped<IButacaService, ButacaService>();

// Construir la aplicación con los servicios registrados.
WebApplication app = builder.Build();

// Configurar el procesamiento de peticiones.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler(
        "/Error",
        createScopeForErrors: true);

    app.UseHsts();
}

app.UseStatusCodePagesWithReExecute(
    "/not-found",
    createScopeForStatusCodePages: true);

app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();
app.UseAntiforgery();

app.MapStaticAssets();

app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.MapSesionEndpoints();

app.Run();