import random
import math

def getRandom( gamma):
    x =  math.log(random.random()) / -gamma
    return x*10

for i in range(10):
     print(getRandom(.8))

