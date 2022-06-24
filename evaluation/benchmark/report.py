import matplotlib.pyplot as plt
import numpy as np

MARK = "#"
data = {}
plot_name = "plot.svg"

with open('bench.txt','r') as bench:
    key = None
    for row in bench:
        row = row.strip()
        if row.startswith(MARK):
            key = row[1:]
        else:
            row = float(row)

            if key in data:
                data[key].append(row)
            else:
                data[key] = [row]

keys = list(data.keys())
values = list(data.values())

fig = plt.figure(figsize =(10, 5))

plt.ylabel("Wall time (seconds)")
x = plt.boxplot(values, labels=keys)

fig.tight_layout()
plt.savefig(plot_name, bbox_inches="tight", format="svg")

values = np.array(values)
# with open("report.md", "w") as f:
#     def wl(line=""):
#         f.write(f"{line}\n")
#
#     wl(f"# Benchmark results ({values.shape[1]} runs)")
#     wl(f"![box plot]({plot_name})")
#     for (k, v) in zip(keys, values):
#         wl(f"## {k}")
#         wl(f"Median: {np.around(np.median(v), 2)}s")
#         wl(f"Variance: {np.around(np.var(v), 2)}s")

print(f"Benchmark results ({values.shape[1]} runs)")
print()
for (k, v) in zip(keys, values):
    print(f"{k}:")
    print(f"Median: {np.around(np.median(v), 2)}s")
    print(f"Variance: {np.around(np.var(v), 4)}s")
    print()
print(f"Box plot: {plot_name}")