
# IN5020 - Remote Method Invocation

Repository for the first assignment of IN5020, by group 7.

# Project layout
A report is provided under `docs/report.pdf` providing an extensive explanation of the project. A slightly draft-README is also found under `docs/`.

# Running

To run the project, run `make purge sim_server`, boot up some client queries with `make sim_client`. After completion, they should both close on their own, no further action needed.

That said: if clients take too long to finish, consider `^C`'ing the client session. Afterwards, you can `make sim_stop` to make sure the proxy server and the simulation servers close cleanly, as they would on a complete client simulation.

You can also replace `fullInputFile` with `shortInputFile` on `parse()` in `SimulateClient.main()` to use a smaller simulation.

# Graphs and logs

Precise logging is provided under the `log/` directory spread over different files depending on its context. Using `grep -R -e 'turnaround' log/`, as an example, is useful to find specific details one wants to query.

After having run a simulation, one can generate plots with a helper script:
```sh
python -m venv venv
. ./venv/bin/activate
pip install numpy matplotlib

python src/graphs.py
```
