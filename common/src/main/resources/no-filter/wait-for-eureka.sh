#!/usr/bin/env bash

if [ -z $EUREKA_HOST ]
then
    echo "Set EUREKA_HOST (e.g. 127.0.0.1)"
    exit 1
fi

# REQUIRED_SERVICES=app1,app2,app3
IFS=',' read -ra serviceIds <<< $REQUIRED_SERVICES
eurekaUrl=http://${EUREKA_HOST}:8761
attempt=0
maxWaitInSeconds=180
intervalInSeconds=3
let maxAttempts=maxWaitInSeconds/intervalInSeconds

# Wait for Eureka server to come up
while true
do
    curl -f ${eurekaUrl}/eureka/apps > /dev/null 2>&1
    if [ $? -eq 0 ]
    then
        echo "Found Eureka server at ${eurekaUrl}"
        break
    else
        let attempt=${attempt}+1
        if [ ${attempt} -gt ${maxAttempts} ]
        then
            echo "Waited too long for Eureka server to come up at ${eurekaUrl}"
            exit 1
        fi
        sleep ${intervalInSeconds}
    fi
done

# Wait for services to come up and be registered with Eureka
while true
do
    failedServices=()

    # Check whether the services have registered with Eureka yet
    for serviceId in "${serviceIds[@]}"
    do
        curl -f ${eurekaUrl}/eureka/apps/${serviceId} > /dev/null 2>&1

        # Add unavailable services to failedServices
        if [ $? -ne 0 ]
        then
            failedServices+=(${serviceId})
        fi
    done

    # If no services failed (i.e. all have been registered with Eureka)
    if [ ${#failedServices[@]} -eq 0 ]
    then
        echo "Found all required services: ${serviceIds[@]}"
        exit 0
    else
        let attempt=${attempt}+1
        if [ ${attempt} -gt ${maxAttempts} ]
        then
            echo "Waited for too long services to register with Eureka: ${failedServices[@]}"
            exit 1
        fi
        sleep ${intervalInSeconds}
    fi
done

