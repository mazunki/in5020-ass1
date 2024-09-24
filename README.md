
# Running

To run the project, run `make purge sim_server`, boot up some client queries with `make sim_client`. After completion, they should both close on their own, no further action needed.

That said: if clients take too long to finish, consider `^C`'ing the client session. Afterwards, you can `make sim_stop` to make sure the proxy server and the simulation servers close cleanly, as they would on a complete client simulation.

You can also replace `fullInputFile` with `shortInputFile` on `parse()` in `SimulateClient.main()` to use a smaller simulation.

# Graphs

After having run a simulation, generate plots as follows
```sh
python -m venv venv
. ./venv/bin/activate
python src/graphs.py
```
