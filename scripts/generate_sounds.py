import numpy as np
from scipy.io import wavfile
import os

def generate_collision_sound(duration, frequency, decay, sample_rate=44100):
    """Generiert einen Kollisionssound mit einem gedämpften Sinus."""
    t = np.linspace(0, duration, int(sample_rate * duration))
    decay_envelope = np.exp(-decay * t)
    signal = np.sin(2 * np.pi * frequency * t) * decay_envelope
    return (signal * 32767).astype(np.int16)

def generate_sounds():
    """Generiert alle benötigten Soundeffekte."""
    os.makedirs("resources/sounds", exist_ok=True)
    
    # Tennis Ball Sounds
    wavfile.write("resources/sounds/tennis_soft.wav", 44100,
                 generate_collision_sound(0.1, 1000, 40))
    wavfile.write("resources/sounds/tennis_hard.wav", 44100,
                 generate_collision_sound(0.15, 1200, 30))
    wavfile.write("resources/sounds/tennis_start.wav", 44100,
                 generate_collision_sound(0.05, 800, 50))
    
    # Billiard Ball Sounds
    wavfile.write("resources/sounds/billiard_soft.wav", 44100,
                 generate_collision_sound(0.1, 2000, 60))
    wavfile.write("resources/sounds/billiard_hard.wav", 44100,
                 generate_collision_sound(0.15, 2500, 50))
    wavfile.write("resources/sounds/billiard_start.wav", 44100,
                 generate_collision_sound(0.05, 1800, 70))
    
    # Bowling Ball Sounds
    wavfile.write("resources/sounds/bowling_soft.wav", 44100,
                 generate_collision_sound(0.2, 300, 20))
    wavfile.write("resources/sounds/bowling_hard.wav", 44100,
                 generate_collision_sound(0.3, 400, 15))
    wavfile.write("resources/sounds/bowling_start.wav", 44100,
                 generate_collision_sound(0.1, 200, 25))

if __name__ == "__main__":
    generate_sounds() 