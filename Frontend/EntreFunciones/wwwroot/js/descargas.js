window.entreFunciones = window.entreFunciones || {};

window.entreFunciones.descargarStream = async (
    nombre,
    tipoContenido,
    referenciaContenido) => {
    const bytes = await referenciaContenido.arrayBuffer();
    const url = URL.createObjectURL(new Blob([bytes], { type: tipoContenido }));
    const enlace = document.createElement("a");
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    URL.revokeObjectURL(url);
};
