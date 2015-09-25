
Streaming demo
==============

Data setup
----------

- Create directory structure using following commands :-
     ```sh
     $ mkdir -p /usr/local/data/tmt/frames/input
     $ mkdir -p /usr/local/data/tmt/frames/output
     $ mkdir -p /usr/local/data/tmt/movies/input
     $ mkdir -p /usr/local/data/tmt/movies/output
     ```

- Copy images in `/usr/local/data/tmt/frames/input` directory.

  Use ffmpeg to chop a video into thousands of images of around 100K size

- Copy movies in `/usr/local/data/tmt/movies/input` directory.

  Copy movies or any large files of around 500MB/file in this dir


Running
-------

- Enter sbt shell by ./activator and run reStart
- Go to browser at http://localhost:6001 and click only button that page
- Also run one test at a time like
  - testOnly *OneToOneTransferTest -- ex frame-bytes
  - testOnly *OneToOneTransferTest -- ex blob-pipelined
  - testOnly *OneToOneTransferTest -- ex blob-pipelined
  - testOnly *OneToManyTransferTest -- ex blob-pipelined

