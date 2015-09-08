Worldpay Downloader
===================

[![Build Status](https://travis-ci.org/hmrc/worldpay-downloader.svg)](https://travis-ci.org/hmrc/worldpay-downloader) [ ![Download](https://api.bintray.com/packages/hmrc/releases/worldpay-downloader/images/download.svg) ](https://bintray.com/hmrc/releases/worldpay-downloader/_latestVersion)

Microservice responsible for the download of Worldpay EMIS report, and well as the streaming of the data to RCS (a HOD).

## Compile the application

To compile the application and run the tests execute

```
sbt clean test it:test fun:test
```


## Run the application

To run the application execute

```
sbt 'run 9051'
```

and then access the application at

```
http://localhost:9051/ping/ping
```

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

-----------------------------------------
API
-----------------------------------------

Download & Process EMIS Report
-----------------------------------------

```
POST        /download/:downloadDate
```

downloadDate: Format is yyyyMMdd (e.g. 20150401 for 1 April, 2015).
chunkSize: (Optional, Integer). The size of the chunks that we send to RCS.
force: (Optional, Boolean, false). Forcing means that we send the file to RCS, even
if they've already received it. Use with caution.

Get Next Scheduled Date 
-----------------------------------------

```
GET         /nextScheduledDate
```

Retrieves the next scheduled date and time for downloading & processing. This is
supposed to every day except for Bank Holidays; the date is of the format: dd/MM/yyyy HH:mm (e.g.
01/04/2015 18:42 for 1 April, 2015. 

If there is a scheduled date, we return it as text, with a status code of 200; otherwise, we
return 404.

Get Current Lock Status
-----------------------------------------

```
GET         /lock 
```

We use the Lock Service, with a lock named 'worldpay-downloader', to ensure that only one instance
is processing a file at a time. If this lock currently exists, we return 200; otherwise, we return 404.

Delete Current Lock
-----------------------------------------

```
DELETE         /lock 
```

Attempt to delete the lock named 'worldpay-downloader'. If successfully deleted - by the Lock Service -
we'll receive a 200; otherwise, a 500 - even if the lock simply didn't exist.


Find Out If A File Has Been Already Processed
-----------------------------------------

```
GET         /processed/:downloadDate
```

If the file has already been processed - that is, downloaded from Worldpay and sent to RCS - then
we'll receive a code of 200; otherwise, we'll receive 404. 

downloadDate: Format is yyyy-MM-dd (e.g. 2015-04-17 for 17 April, 2015).

Request That A File Be Processed
-----------------------------------------

```
POST        /processed/:downloadDate
```

This is similar to the /download/:downloadDate endpoint above, with the important difference
that this one ensures that the lock is available. Note that there is no 'force' option here, so
we cannot process for a file that's already been processed.

If we cannot get the lock, the response will be 409; if the lock is granted, but that file has
already been processed: 400; otherwise, if successful: 200.

Request That The Scheduler Be Re-booted
-----------------------------------------

```
POST        /scheduler
```

If we suspect that the Scheduler (Akka) is no longer running, we can 're-boot' it. But please note
that if the Scheduler is actually still running, and we do this, the behaviour is unknown; so, use with caution.