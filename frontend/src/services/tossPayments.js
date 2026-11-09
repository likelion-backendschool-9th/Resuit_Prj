// npm 패키지 대신 공식 스크립트를 쓰므로 package.json과 프론트 이미지를 바꿀 필요가 없다.
const SDK_URL = 'https://js.tosspayments.com/v2/standard';

let loading = null;

// 스크립트를 한 번만 넣고, 전역 TossPayments 함수를 돌려준다. 브라우저에서만 호출한다(렌더링 중 호출 금지).
function loadSdk() {
  if (window.TossPayments) return Promise.resolve(window.TossPayments);
  if (!loading) {
    loading = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = SDK_URL;
      script.async = true;
      script.onload = () => (window.TossPayments ? resolve(window.TossPayments) : reject(new Error('결제 모듈을 불러오지 못했어요.')));
      script.onerror = () => {
        loading = null;
        reject(new Error('결제 모듈을 불러오지 못했어요.'));
      };
      document.head.appendChild(script);
    });
  }
  return loading;
}

export async function requestTossPay(order) {
  const TossPayments = await loadSdk();
  const payment = TossPayments(order.clientKey).payment({ customerKey: order.customerKey });
  const origin = window.location.origin;
  await payment.requestPayment({
    method: 'CARD',
    amount: { currency: 'KRW', value: order.amount },
    orderId: order.orderId,
    orderName: order.orderName,
    successUrl: `${origin}/credits/success`,
    failUrl: `${origin}/credits/fail`,
    // 카드 선택 없이 토스페이 간편결제 화면으로 바로 연다.
    card: { flowMode: 'DIRECT', easyPay: 'TOSSPAY' },
  });
}
