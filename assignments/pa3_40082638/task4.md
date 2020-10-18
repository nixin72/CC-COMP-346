# Task 4

My solution uses a second class to represent each chopstick and track the state of each chopstick. 
This makes it much easier to make sure that starvation never occurs because it allows you to keep track of who last used the chopsticks. 
When philosopher picks up a chopstick, their ID gets recorded as the last user of the chopsticks, and while they're the last user they can't eat again.
This will make sure that other philosophers don't starve by another one eating over and over again.
As soon as a philosopher is done eating, they drop both their chopsticks and notify that others can eat.