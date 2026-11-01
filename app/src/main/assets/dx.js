(function () {
  if (window.__dx) return;
  var AC = window.AudioContext || window.webkitAudioContext;
  var D = window.__dx = { v: { mic: 50, vol: 50, bass: 50, mid: 50, treble: 50 }, outs: [], mics: [] };

  function lvl(k) {
    var p = D.v[k];
    if (k === 'mic') return 1 + p / 100 * 19;
    if (k === 'vol') return 1 + p / 100 * 9;
    if (k === 'bass') return p / 100 * 20;
    if (k === 'mid') return p / 100 * 12;
    return p / 100 * 15;
  }
  function apply() {
    D.mics.forEach(function (g) { g.gain.value = lvl('mic'); });
    D.outs.forEach(function (o) {
      o.g.gain.value = lvl('vol');
      o.b.gain.value = lvl('bass');
      o.m.gain.value = lvl('mid');
      o.t.gain.value = lvl('treble');
    });
  }
  D.set = function (k, p) { D.v[k] = p; apply(); };
  D.setAll = function (o) { for (var k in o) D.v[k] = o[k]; apply(); };

  var md = navigator.mediaDevices;
  if (md && md.getUserMedia && AC) {
    var orig = md.getUserMedia.bind(md);
    md.getUserMedia = function (c) {
      return orig(c).then(function (stream) {
        if (!stream.getAudioTracks().length) return stream;
        try {
          var ac = new AC();
          var src = ac.createMediaStreamSource(stream);
          var g = ac.createGain();
          var comp = ac.createDynamicsCompressor();
          var dst = ac.createMediaStreamDestination();
          src.connect(g); g.connect(comp); comp.connect(dst);
          D.mics.push(g); apply(); ac.resume();
          var out = dst.stream;
          stream.getVideoTracks().forEach(function (t) { out.addTrack(t); });
          return out;
        } catch (e) { return stream; }
      });
    };
  }

  function hook(el, s) {
    if (el.__dxh || !AC) return;
    el.__dxh = 1;
    try {
      var ac = new AC();
      var src = ac.createMediaStreamSource(s);
      var b = ac.createBiquadFilter(); b.type = 'lowshelf'; b.frequency.value = 200;
      var m = ac.createBiquadFilter(); m.type = 'peaking'; m.frequency.value = 1000;
      var t = ac.createBiquadFilter(); t.type = 'highshelf'; t.frequency.value = 3500;
      var g = ac.createGain();
      var comp = ac.createDynamicsCompressor();
      src.connect(b); b.connect(m); m.connect(t); t.connect(g); g.connect(comp); comp.connect(ac.destination);
      D.outs.push({ g: g, b: b, m: m, t: t });
      apply(); ac.resume();
      el.muted = true;
    } catch (e) { el.__dxh = 0; }
  }
  var d = Object.getOwnPropertyDescriptor(HTMLMediaElement.prototype, 'srcObject');
  if (d && d.set) {
    Object.defineProperty(HTMLMediaElement.prototype, 'srcObject', {
      configurable: true, enumerable: d.enumerable,
      get: function () { return d.get.call(this); },
      set: function (s) {
        d.set.call(this, s);
        try {
          if (s && s.getAudioTracks) {
            var el = this;
            if (s.getAudioTracks().length) hook(el, s);
            else s.addEventListener('addtrack', function () { if (s.getAudioTracks().length) hook(el, s); });
          }
        } catch (e) {}
      }
    });
  }
})();
