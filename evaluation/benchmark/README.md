# Benchmark

## Run the benchmark

```shell
make bench
```

## Report

Prerequisites:
- Python 3
- Matplotlib

### Prepare environment
(Skip if you already have a suitable python environment)
```shell
pip install virtualenv
virtualenv -p python3 venv
source venv/bin/activate
pip install matplotlib
```
> Note: `deactivate` to exit the virtual env
> 
### Generate the report
```shell
make report
```

