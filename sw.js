const CACHE='sud-ijro-v27';
const SELF_URL = self.location.pathname.replace(/sw\.js$/, 'index.html');

self.addEventListener('install', e=>{
  e.waitUntil(caches.open(CACHE).then(c=>c.add(SELF_URL).catch(()=>{})));
  self.skipWaiting();
});

self.addEventListener('activate', e=>{
  e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)))));
  self.clients.claim();
});

self.addEventListener('fetch', e=>{
  if(e.request.method!=='GET') return;
  e.respondWith(
    caches.open(CACHE).then(cache=>
      cache.match(e.request).then(cached=>{
        const fresh = fetch(e.request).then(res=>{
          if(res && res.status===200) cache.put(e.request, res.clone());
          return res;
        }).catch(()=>cached);
        return cached || fresh;
      })
    )
  );
});
