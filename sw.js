const CACHE='sud-ijro-v68';
const SELF_URL = self.location.pathname.replace(/sw\.js$/, 'index.html');

self.addEventListener('install', e=>{
  // {cache:'reload'} — brauzerning oddiy HTTP keshini chetlab o'tib,
  // serverdan albatta ENG YANGI index.html'ni olib, shu versiyaga saqlaydi.
  e.waitUntil(
    fetch(SELF_URL, { cache: 'reload' })
      .then(res => caches.open(CACHE).then(c => c.put(SELF_URL, res)))
      .catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener('activate', e=>{
  e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)))));
  self.clients.claim();
});

self.addEventListener('fetch', e=>{
  if(e.request.method!=='GET') return;
  // TARMOQ-BIRINCHI (network-first): internet bor bo'lsa, HAR DOIM eng
  // yangi faylni ko'rsatadi va uni keshga yangilab qo'yadi. Faqat internet
  // uzilib qolganda (oflayn), eng oxirgi saqlangan nusxa ishlatiladi.
  e.respondWith(
    fetch(e.request).then(res=>{
      if(res && res.status===200){
        const resClone = res.clone();
        caches.open(CACHE).then(cache=>cache.put(e.request, resClone));
      }
      return res;
    }).catch(()=>caches.open(CACHE).then(cache=>cache.match(e.request)))
  );
});
