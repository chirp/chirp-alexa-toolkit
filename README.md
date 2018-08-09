# Chirp Alexa Toolkit node.js and python lambda sample functions

## Usage
Each of the platforms contains a file with a function that is designed to be used on lambda.
To use these functions on lambda you need to create a zip folder which contains the function file 
in the root folder and a all the dependencies. 


### Create a build zip file
```
./build.sh
```

### Install dependencies (For local development)
#### For python function
```
cd python
pip install -r requirements.txt -t ./
```

> NOTE: If you have "must supply either home or prefix/exec-prefix -- not both" error when installing dependencies with pip,
> just edit `~/.pydistutils.cfg` and paste the following content:

```
[install]
prefix=
```

> Source: http://stackoverflow.com/questions/24257803/distutilsoptionerror-must-supply-either-home-or-prefix-exec-prefix-not-both/24357384


#### For node.js function

```
cd nodejs
npm install
```

### Zip node.js or python

```
cd {platform}/
zip -r ../alexa-{platform}.zip *
```
