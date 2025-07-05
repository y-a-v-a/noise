const gd = require('node-gd');
const ImprovedNoise = require('./ImprovedNoise');
const noiz = require('./other-noise').Noise2D;

//console.dir(new ImprovedNoise());
//console.log(ImprovedNoise.noise(1,2,3));

for (let i = 0; i < 100; i++) {
  let noise = ImprovedNoise.noise(
    Math.random() * Math.PI * 500,
    Math.random() * 11 * Math.PI * 500,
    Math.random() * 6,
  );
  //console.log(.5 + .25 * noise / (1 << 16));
  //console.log(noise);
}

//console.log(gd);

gd.createTrueColor(500, 500, (error, img) => {
  for (let x = 0; x < 500; x++) {
    for (let y = 0; y < 500; y++) {
      let r = Math.sin(Math.random() * 500 * Math.PI);
      let rc = Math.cos(Math.random() * 500 * Math.PI);
      let noise = ImprovedNoise.noise(x, y * r, 8);

      let noiseR = 0.5 + (0.25 * noise) / (1 << 16);
      let noiseQ = 0.5 + 0.25 * noise;
      //console.log(1000000*(.25 * noise / (1 << 16)));

      //let noise = ImprovedNoise.noise(x, y, Math.random());
      let channelValueR = 127 + 1000000 * ((0.25 * noise) / (1 << 16));
      let channelValueG = 127 + 1000000 * ((0.25 * noise) / (1 << 16));
      let channelValueB = 127 + 1000000 * ((0.25 * noise) / (1 << 16));
      //let channelValue = Math.random() * 127 + 127;
      let color = gd.trueColor(
        ~~channelValueR,
        ~~channelValueG,
        ~~channelValueB,
      );
      //console.log(color);
      img.setPixel(x, y, color);
    }
  }

  img.saveJpeg(`./test-${Date.now()}.jpg`, 100, function (error) {
    if (error) throw error;
    img.destroy();
  });
});
