# Distributed System Project - Reliable Broadcast Library

## Description:

This project is focused on the implementation of a library for reliable broadcast communication among a set of faulty processes together with a simple application to test it.
The library must guarantee **virtual synchrony**, while ordering should be at least **FIFO**.

Our project consists of a real distributed application implemented in ***Java***, with the use of ***Docker*** for the deployment and sockets as basic communication facilities.

## Assumptions: 

Following the project specifications, we considered a LAN scenario (i.e., link-layer broadcast is available), that no joins or disconnections happen during the view change and in general, that no processes fail during the time required for previous failures to be recovered (i.e. viewchanges proceed smoothly) and ack messages are always received by all or none

## More details on:

- **Design Choices** available in the [Presentation Doc](https://github.com/LorenzoMainetti/distributed-systems-project-2022-fusco-galzerano-mainetti/blob/main/docs/Presentation.pdf)

- **Specifications** available in the [Specification Doc](https://github.com/LorenzoMainetti/distributed-systems-project-2022-fusco-galzerano-mainetti/blob/main/docs/projects%20specification.pdf)


## Team

[Luigi Fusco](https://github.com/luigifusco) 
[Arianna Galzerano](https://github.com/arigalzi) 
[Lorenzo Mainetti](https://github.com/LorenzoMainetti)
