# Importing EdgeSimPy components
from edge_sim_py import *

# Reading dataset from URL
print("\n>>>>> Reading dataset from URL <<<<<\n")
## Creating a Simulator object
simulator = Simulator()
## Loading the dataset file from the external JSON file
simulator.initialize(input_file="https://raw.githubusercontent.com/EdgeSimPy/edgesimpy-tutorials/master/datasets/sample_dataset1.json")
## Displaying some of the objects loaded from the dataset
for user in User.all():
    print(f"{user}. Coordinates: {user.coordinates}")

# Reading dataset from python dict
print("\n>>>>> Reading dataset from python dict <<<<<\n")
## Creating a Python dictionary representing a sample dataset with a couple of users
my_simplified_dataset = {
    "User": [
        {
            "attributes": {
                "id": 1,
                "coordinates": [
                    1,
                    1
                ]
            },
            "relationships": {}
        },
        {
            "attributes": {
                "id": 2,
                "coordinates": [
                    3,
                    3
                ]
            },
            "relationships": {}
        },
        {
            "attributes": {
                "id": 3,
                "coordinates": [
                    5,
                    1
                ]
            },
            "relationships": {}
        },
        {
            "attributes": {
                "id": 4,
                "coordinates": [
                    0,
                    0
                ]
            },
            "relationships": {}
        }
    ]
}
## Creating a Simulator object
simulator_2 = Simulator()
## Loading the dataset from the dictionary "my_simplified_dataset"
simulator_2.initialize(input_file=my_simplified_dataset)
## Displaying the objects loaded from the dataset
for user in User.all():
    print(f"{user}. Coordinates: {user.coordinates}")