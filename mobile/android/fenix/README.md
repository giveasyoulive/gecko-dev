## Donation Reminder

The donation reminder base code is a fork of the project mozilla-mobile/fenix

The physical code changes are kept in this branch "donation_reminder" in order for the developer to review fenix updates after any fork synchronisation.

## Donation Reminder Extension

The donation reminder extension repository needs to be installed in /var/everyclick/development/donation-reminder.

Follow the readme installation instructions and then run ./build firefox to create a distribution version of the extension.

Gradle picks up the latest files from the distribution directory

## Initial Setup/Config

Please install git-cinnabar onto your mac / pc via brew.

Link git-cinnabar to the latest Gecko mecurial repository by running the following commands:

cd gecko-dev

git remote add central hg::https://hg.mozilla.org/mozilla-central -t branches/default/tip
git config remote.central.fetch +refs/heads/branches/default/tip:refs/remotes/central/default

git remote add central hg::https://hg.mozilla.org/mozilla-central -t branches/default/tip
git remote update

Run the following command to ensure that fast build mode (Artifact Mode) is used.   

./mach --no-interactive bootstrap --application-choice="GeckoView/Firefox for Android Artifact Mode"

Then issue the build.
./mach build

Once this is done then run android studio and open mobile/android/fenix - it will take a long time to initialise.

Once started, select the build variant using Build -> Select Build variant.

Note:  You need to configure local.properties before running your build.  Please add the following security items:

  build.storePassword
  build.keyPassword
  build.keyAlias
  build.storeFile
  advert.authorization
  advert.username
  advert.password
  advert.deviceSecurityCode
 
## Build variants

Please use the following variants for the donation reminder project. You can select the variant under the menu Build - select build variant

debug
release

## gradle.properties
Please add the version of the donation reminder to this file:
VersionName=2.0.0000

## local.properties
If you are running debug builds add the following line:
debuggable=true

## Signing
Keys and passwords are not stored in this repository.  You will need to create your own to deploy the Donation Reminder.


## License
This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at http://mozilla.org/MPL/2.0/

