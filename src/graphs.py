#!/usr/bin/env python
import re

import matplotlib.pyplot as plt
import numpy as np

client_log = 'log/client_sim.log'
server_log = 'log/server_server.log'

def fetch(log_file, pattern):
    times = []
    with open(log_file, 'r') as file:
        for line in file:
            match = re.search(pattern, line)
            if match:
                times.append([int(m) for m in match.groups()])
    return np.array(times)

execution_times = fetch(server_log, r'execution time: (\d+)ms')
waiting_times = fetch(server_log, r'waiting time: (\d+)ms')
turnaround_times = fetch(client_log, r'turnaround time: (\d+)ms')

def plot(ys, name):
    xs = np.arange(len(ys))
    plt.figure(figsize=(10, 6))

    plt.plot(xs, ys, label=f'{name} (ms)', marker='o')

    plt.title(f'{name} over Requests')
    plt.xlabel('Request Number')
    plt.ylabel('Time (ms)')
    plt.legend()
    plt.grid(True)

    plt.savefig(f'{name}.png')
    plt.close()

plot(execution_times, 'Execution Time')
plot(waiting_times, 'Waiting Time')
plot(turnaround_times, 'Turnaround Time')

