FROM ubuntu:latest
LABEL authors="macfo"

ENTRYPOINT ["top", "-b"]