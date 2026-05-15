import os
import sys
import time
from pathlib import Path
from typing import List


# =========================================================
# CONFIGURACIÓN
# =========================================================
PROJECT_DIR = r"C:\Users\WinterOS\IdeaProjects\CalidadSoftwareMS3"
OUTPUT_DIR = r"C:\Users\WinterOS\Desktop"
OUTPUT_FILENAME = "codigo_unificado_springboot.txt"

INCLUDE_EXTENSIONS = {".java"}
INCLUDE_FILENAMES = {"application.properties"}

EXCLUDED_DIRS = {
    ".git",
    ".idea",
    "target",
    "build",
    "out",
    ".mvn",
    "node_modules",
    "__pycache__"
}

PROGRESS_BAR_LENGTH = 40
MAX_FILE_DISPLAY_LENGTH = 130
MIN_RENDER_INTERVAL = 0.10  # segundos


# =========================================================
# UTILIDADES
# =========================================================
def enable_ansi_on_windows() -> None:
    """
    Habilita secuencias ANSI en Windows para poder mover el cursor
    sin limpiar toda la consola.
    """
    if os.name != "nt":
        return

    try:
        import ctypes
        kernel32 = ctypes.windll.kernel32
        handle = kernel32.GetStdHandle(-11)  # STD_OUTPUT_HANDLE = -11
        mode = ctypes.c_uint32()
        if kernel32.GetConsoleMode(handle, ctypes.byref(mode)):
            ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004
            kernel32.SetConsoleMode(handle, mode.value | ENABLE_VIRTUAL_TERMINAL_PROCESSING)
    except Exception:
        # Si falla, el script sigue funcionando igual.
        pass


def format_seconds(seconds: float) -> str:
    if seconds < 0:
        seconds = 0

    seconds = int(seconds)
    h = seconds // 3600
    m = (seconds % 3600) // 60
    s = seconds % 60

    if h > 0:
        return f"{h:02d}:{m:02d}:{s:02d}"
    return f"{m:02d}:{s:02d}"


def should_include_file(file_path: Path) -> bool:
    if file_path.name in INCLUDE_FILENAMES:
        return True
    if file_path.suffix.lower() in INCLUDE_EXTENSIONS:
        return True
    return False


def discover_files(base_dir: Path) -> List[Path]:
    found_files: List[Path] = []

    for root, dirs, files in os.walk(base_dir):
        dirs[:] = [d for d in dirs if d not in EXCLUDED_DIRS]

        root_path = Path(root)
        for file_name in files:
            file_path = root_path / file_name
            if should_include_file(file_path):
                found_files.append(file_path)

    found_files.sort(key=lambda p: str(p).lower())
    return found_files


def read_file_with_fallback(file_path: Path) -> str:
    encodings = ["utf-8", "utf-8-sig", "cp1252", "latin-1"]

    for enc in encodings:
        try:
            return file_path.read_text(encoding=enc)
        except UnicodeDecodeError:
            continue
        except Exception as e:
            return f"[ERROR AL LEER EL ARCHIVO: {e}]"

    return "[NO SE PUDO LEER EL ARCHIVO POR CODIFICACIÓN DESCONOCIDA]"


def truncate_text(text: str, max_length: int) -> str:
    if len(text) <= max_length:
        return text
    return "..." + text[-(max_length - 3):]


def build_progress_bar(current: int, total: int) -> str:
    progress = 1.0 if total <= 0 else current / total
    filled = int(PROGRESS_BAR_LENGTH * progress)
    empty = PROGRESS_BAR_LENGTH - filled
    return "█" * filled + "-" * empty


def calculate_eta(start_time: float, processed: int, total: int) -> float:
    elapsed = time.time() - start_time
    if processed <= 0 or elapsed <= 0:
        return 0.0

    avg_per_file = elapsed / processed
    remaining = total - processed
    return avg_per_file * remaining


# =========================================================
# RENDER DE CONSOLA SIN PARPADEO
# =========================================================
class ConsoleProgress:
    def __init__(self, total_files: int):
        self.total_files = total_files
        self._printed_progress_block = False
        self._last_render_time = 0.0

    def print_initial_header(self) -> None:
        print("=" * 90)
        print("EXTRACTOR DE CÓDIGO SPRING BOOT A TXT")
        print("=" * 90)
        print("Recorriendo el proyecto y filtrando archivos válidos...")
        print()

    def print_total_detected(self) -> None:
        print("=" * 90)
        print("EXTRACTOR DE CÓDIGO SPRING BOOT A TXT")
        print("=" * 90)
        print(f"Total de archivos detectados: {self.total_files}")
        print()

    def init_progress_block(self) -> None:
        """
        Imprime una sola vez el bloque que luego será actualizado.
        """
        print(f"[{'-' * PROGRESS_BAR_LENGTH}]   0.00%  |  0/{self.total_files} archivos")
        print("Tiempo transcurrido: 00:00  |  Tiempo estimado restante: 00:00")
        print()
        print("Archivo actual:")
        print("-")
        self._printed_progress_block = True
        sys.stdout.flush()

    def render(self, processed_files: int, start_time: float, current_file: str, force: bool = False) -> None:
        """
        Actualiza solo el bloque de progreso, sin limpiar toda la consola.
        """
        now = time.time()
        if not force and (now - self._last_render_time) < MIN_RENDER_INTERVAL:
            return

        if not self._printed_progress_block:
            self.init_progress_block()

        percent = (processed_files / self.total_files * 100) if self.total_files > 0 else 100.0
        elapsed = time.time() - start_time
        eta = calculate_eta(start_time, processed_files, self.total_files)
        bar = build_progress_bar(processed_files, self.total_files)
        current_file_display = truncate_text(current_file, MAX_FILE_DISPLAY_LENGTH)

        # Subir 5 líneas y reescribir solo esa sección
        # Línea 1: barra
        # Línea 2: tiempos
        # Línea 3: vacío
        # Línea 4: "Archivo actual:"
        # Línea 5: nombre archivo
        sys.stdout.write("\033[5F")  # subir 5 líneas
        sys.stdout.write("\033[2K")
        sys.stdout.write(f"[{bar}] {percent:6.2f}%  |  {processed_files}/{self.total_files} archivos\n")
        sys.stdout.write("\033[2K")
        sys.stdout.write(
            f"Tiempo transcurrido: {format_seconds(elapsed)}  |  "
            f"Tiempo estimado restante: {format_seconds(eta)}\n"
        )
        sys.stdout.write("\033[2K\n")
        sys.stdout.write("\033[2K")
        sys.stdout.write("Archivo actual:\n")
        sys.stdout.write("\033[2K")
        sys.stdout.write(f"{current_file_display}\n")
        sys.stdout.flush()

        self._last_render_time = now

    def finish(self, total_elapsed: float, output_path: Path) -> None:
        self.render(
            processed_files=self.total_files,
            start_time=time.time() - total_elapsed,
            current_file="Proceso finalizado",
            force=True
        )
        print()
        print(f"Tiempo total: {format_seconds(total_elapsed)}")
        print(f"TXT generado en:\n{output_path}")


# =========================================================
# PROCESO PRINCIPAL
# =========================================================
def main() -> None:
    enable_ansi_on_windows()

    project_path = Path(PROJECT_DIR)
    output_path = Path(OUTPUT_DIR) / OUTPUT_FILENAME

    if not project_path.exists():
        print(f"ERROR: La carpeta del proyecto no existe:\n{project_path}")
        return

    if not project_path.is_dir():
        print(f"ERROR: La ruta indicada no es una carpeta:\n{project_path}")
        return

    console = ConsoleProgress(total_files=0)
    console.print_initial_header()

    files_to_export = discover_files(project_path)
    total_files = len(files_to_export)

    print("=" * 90)
    print("EXTRACTOR DE CÓDIGO SPRING BOOT A TXT")
    print("=" * 90)
    print(f"Total de archivos detectados: {total_files}")
    print()

    if total_files == 0:
        print("No se encontraron archivos .java ni application.properties.")
        return

    # Reconfigurar el progreso con el total correcto
    console = ConsoleProgress(total_files=total_files)
    console.init_progress_block()

    # Si el archivo ya existe, eliminarlo y recrearlo
    if output_path.exists():
        try:
            output_path.unlink()
        except Exception as e:
            print()
            print(f"ERROR: No se pudo eliminar el archivo existente:\n{output_path}\nDetalle: {e}")
            return

    start_time = time.time()

    try:
        with open(output_path, "w", encoding="utf-8", newline="\n") as out_file:
            # Cabecera del TXT
            out_file.write("=" * 120 + "\n")
            out_file.write("CODIGO UNIFICADO DEL PROYECTO SPRING BOOT\n")
            out_file.write("=" * 120 + "\n")
            out_file.write(f"Proyecto origen   : {project_path}\n")
            out_file.write(f"Archivo generado  : {output_path}\n")
            out_file.write(f"Fecha exportación : {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
            out_file.write(f"Total archivos    : {total_files}\n")
            out_file.write("=" * 120 + "\n\n")

            for index, file_path in enumerate(files_to_export, start=1):
                relative_path = file_path.relative_to(project_path)

                console.render(
                    processed_files=index - 1,
                    start_time=start_time,
                    current_file=str(relative_path),
                    force=False
                )

                file_content = read_file_with_fallback(file_path)

                out_file.write("=" * 120 + "\n")
                out_file.write(f"ARCHIVO: {relative_path}\n")
                out_file.write("=" * 120 + "\n\n")
                out_file.write(file_content)
                out_file.write("\n\n")

                # Mantener escritura estable, sin flush agresivo en cada vuelta
                if index % 25 == 0:
                    out_file.flush()

                console.render(
                    processed_files=index,
                    start_time=start_time,
                    current_file=str(relative_path),
                    force=False
                )

            out_file.flush()

        total_elapsed = time.time() - start_time
        console.finish(total_elapsed=total_elapsed, output_path=output_path)

    except PermissionError:
        print()
        print(f"ERROR: No se tienen permisos para crear o escribir el archivo:\n{output_path}")
    except Exception as e:
        print()
        print(f"ERROR inesperado:\n{e}")


if __name__ == "__main__":
    main()
