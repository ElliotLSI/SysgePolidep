SELECT 
    r.Id_Reserva,
    u.Nombre, u.Apellido,
    r.Fecha_Reserva,
    r.Hora_Inicio,
    r.Duracion_Horas,
    r.Estado
FROM reserva r
JOIN usuario u ON r.Id_Usuario = u.IdUsuario
WHERE r.Id_Instalacion = 1 -- ID de la cancha de fútbol
  AND r.Fecha_Reserva = '2026-09-25'
  AND r.Estado != 'CANCELADA'
ORDER BY r.Hora_Inicio ASC;