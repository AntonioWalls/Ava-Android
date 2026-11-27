"""Generate Ava Demo's breathing cues from sine waves and encode them as Vorbis."""

import argparse
import math
import struct
import subprocess
from pathlib import Path

SAMPLE_RATE = 22_050
CYCLES = 12
INHALE_SECONDS = 4
EXHALE_SECONDS = 6
CUE_SECONDS = 1.1


def cue(start_hz: float, end_hz: float) -> bytes:
    samples = bytearray()
    for i in range(round(SAMPLE_RATE * CUE_SECONDS)):
        seconds = i / SAMPLE_RATE
        fraction = seconds / CUE_SECONDS
        envelope = math.sin(math.pi * fraction) ** 2
        phase = math.tau * seconds * (start_hz + (end_hz - start_hz) * fraction / 2)
        tone = math.sin(phase) + 0.12 * math.sin(2 * phase)
        samples.extend(struct.pack("<h", round(0.18 * 32_767 * envelope * tone)))
    return bytes(samples)


def phase(start_hz: float, end_hz: float, seconds: int) -> bytes:
    tone = cue(start_hz, end_hz)
    return tone.ljust(2 * seconds * SAMPLE_RATE, b"\0")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ffmpeg", default="ffmpeg")
    ffmpeg = parser.parse_args().ffmpeg
    output = Path(__file__).resolve().parents[1] / "src/main/res/raw/breathing_cues.ogg"
    output.parent.mkdir(parents=True, exist_ok=True)

    inhale = phase(262, 392, INHALE_SECONDS)
    exhale = phase(330, 220, EXHALE_SECONDS)
    subprocess.run(
        [
            ffmpeg, "-hide_banner", "-loglevel", "error", "-y",
            "-f", "s16le", "-ar", str(SAMPLE_RATE), "-ac", "1", "-i", "pipe:0",
            "-c:a", "libvorbis", "-q:a", "3", str(output),
        ],
        input=(inhale + exhale) * CYCLES,
        check=True,
    )


if __name__ == "__main__":
    main()
