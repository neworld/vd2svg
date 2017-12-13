[![npm](https://img.shields.io/npm/v/vd2svg.svg)](https://www.npmjs.com/package/vd2svg)
[![Build Status](https://travis-ci.org/neworld/vd2svg.svg?branch=master)](https://travis-ci.org/neworld/vd2svg)

### vd2svg

Android vector drawable to SVG converter.
This tool is could help you extract all android vectors to SVG and display in any file browser.

### Installation

Easiest way is to install via [NPM](https://www.npmjs.com/package/vd2svg)
```bash
npm install vd2svg -g
```

Another way is to clone this repo and install localy:

```bash
./gradlew jdeployInstall
```

### Usage

Program skips all non vector files.

```bash
cd ~/your/drawable/folder
vd2svg *.xml
```

If you are using colors from resource files, then need point to colors file

```bash
cd ~/your/drawable/folder
vd2svg -r ../values *.xml
``` 

`-r` scans directory recursively and searches for color values.
It also could point to concrete file.

### Help

```bash
➜  ~ vd2svg --help
usage: vd2svg [-h] [-v] [-q] [-o OUTPUT] [-r RESOURCES]... INPUT...

optional arguments:
  -h, --help              show this help message and exit

  -v, --verbose           Verbose mode. Causes vd2svg to print info messages

  -q, --quiet             Quiet mode. Disables progress output

  -o OUTPUT,              Path to the output directory. By default, output is
  --output OUTPUT         put together with the input files

  -r RESOURCES,           File or directory containing android resources for
  --resources RESOURCES   color parsing


positional arguments:
  INPUT                   Input file mask. For example '*.xml'
```

### License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
