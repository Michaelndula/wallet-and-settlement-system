import React, { useState, useCallback, useEffect, useRef } from 'react';
import axios from 'axios';

// --- SVG Icon Components ---
const WalletIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 12V8H6a2 2 0 0 1-2-2c0-1.1.9-2 2-2h12v4"></path><path d="M4 6v12c0 1.1.9 2 2 2h14v-4"></path><path d="M18 12a2 2 0 0 0-2 2c0 1.1.9 2 2 2h4v-4h-4z"></path></svg>;
const ReportIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><line x1="10" y1="9" x2="8" y2="9"></line></svg>;
const DownloadIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>;
const LoadingSpinner = () => <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>;


// --- UI Components ---
const Card = ({ children, className }) => (
  <div className={`bg-white shadow-lg rounded-2xl p-6 md:p-8 ${className} transition-all duration-300 hover:shadow-xl`}>
    {children}
  </div>
);

const SectionTitle = ({ icon, children }) => (
  <div className="flex items-center text-2xl font-bold text-gray-800 mb-6 border-b-2 border-gray-200 pb-3">
    <div className="mr-3 text-indigo-600">{icon}</div>
    {children}
  </div>
);

const Input = React.forwardRef((props, ref) => (
  <input
    {...props}
    ref={ref}
    className="w-full px-4 py-3 mt-2 text-gray-700 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
  />
));

const Button = ({ children, onClick, className = '', isLoading = false, icon = null }) => (
  <button
    onClick={onClick}
    disabled={isLoading}
    className={`w-full flex items-center justify-center px-4 py-3 mt-4 font-semibold text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 transition duration-300 ease-in-out transform hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
  >
    {isLoading ? <LoadingSpinner /> : <><span className="mr-2">{icon}</span>{children}</>}
  </button>
);

const MessageBox = ({ message, type }) => {
  if (!message) return null;
  const typeClasses = {
    success: 'bg-green-100 text-green-800 border-l-4 border-green-500',
    error: 'bg-red-100 text-red-800 border-l-4 border-red-500',
  };
  return <div className={`p-4 my-4 rounded-lg shadow-sm ${typeClasses[type]}`}>{message}</div>;
};

const StatCard = ({ title, value, color }) => (
    <div className={`p-4 rounded-lg shadow-inner`}>
        <p className="text-sm font-medium text-gray-500">{title}</p>
        <p className={`text-2xl font-bold ${color}`}>{value}</p>
    </div>
);


// --- Main App Component ---
function App() {
  // State
  const [walletId, setWalletId] = useState('12345');
  const [amount, setAmount] = useState('100.00');
  const [balance, setBalance] = useState(null);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [isLoading, setIsLoading] = useState({ balance: false, topup: false, consume: false, recon: false });

  const [reconDate, setReconDate] = useState('2025-09-12');
  const [reconReport, setReconReport] = useState(null);

  const walletIdInputRef = useRef(null);

  const showMessage = (msg, type) => {
    setMessage(msg);
    setMessageType(type);
    setTimeout(() => setMessage(''), 5000);
  };

  const handleGetBalance = useCallback(async () => {
    if (!walletId) {
      showMessage('Please enter a Wallet ID.', 'error');
      walletIdInputRef.current.focus();
      return;
    }
    setIsLoading(prev => ({ ...prev, balance: true }));
    setBalance(null);
    try {
      const response = await axios.get(`/api/v1/wallets/${walletId}/balance`);
      setBalance(`Balance: ${Number(response.data).toFixed(2)}`);
      showMessage('Balance fetched successfully!', 'success');
    } catch (error) {
      setBalance(null);
      const errorMsg = error.response?.data?.message || 'Failed to fetch balance. The wallet may not exist yet.';
      showMessage(errorMsg, 'error');
    } finally {
      setIsLoading(prev => ({ ...prev, balance: false }));
    }
  }, [walletId]);

  useEffect(() => {
    handleGetBalance();
  }, [handleGetBalance]);

  const handleTransaction = async (type) => {
    if (!walletId || !amount) {
      showMessage('Please enter both Wallet ID and Amount.', 'error');
      return;
    }
    const action = type === 'topup' ? 'Top-up' : 'Consumption';
    setIsLoading(prev => ({ ...prev, [type]: true }));
    try {
      const payload = { amount: parseFloat(amount), transactionId: `${type}-${Date.now()}` };
      await axios.post(`/api/v1/wallets/${walletId}/${type}`, payload);
      showMessage(`${action} successful!`, 'success');
      handleGetBalance(); // Refresh balance
      setAmount('');
    } catch (error) {
      const errorMsg = error.response?.data?.message || `${action} failed.`;
      showMessage(errorMsg, 'error');
    } finally {
       setIsLoading(prev => ({ ...prev, [type]: false }));
    }
  };

  const handleReconciliation = async () => {
    if (!reconDate) {
      showMessage('Please select a date.', 'error');
      return;
    }
    setIsLoading(prev => ({...prev, recon: true}));
    setReconReport(null);
    try {
      const response = await axios.get(`/api/v1/reconciliation/report?date=${reconDate}`);
      setReconReport(response.data);
      showMessage('Reconciliation report generated.', 'success');
    } catch (error) {
       setReconReport(null);
      const errorMsg = error.response?.data?.message || 'Failed to generate report. Ensure the CSV file exists for this date.';
      showMessage(errorMsg, 'error');
    } finally {
        setIsLoading(prev => ({...prev, recon: false}));
    }
  };

  const handleExport = () => {
    window.location.href = `/api/v1/reconciliation/report/csv?date=${reconDate}`;
  };

  return (
    <div className="bg-gray-50 min-h-screen font-sans text-gray-800">
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Wallet & Settlement System</h1>
        </div>
      </header>

      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <MessageBox message={message} type={messageType} />

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-4">
          {/* Wallet Management Card */}
          <Card>
            <SectionTitle icon={<WalletIcon />}>Wallet Operations</SectionTitle>
            <div>
              <label className="font-semibold text-gray-600">Wallet ID</label>
              <Input ref={walletIdInputRef} type="text" value={walletId} onChange={(e) => setWalletId(e.target.value)} placeholder="e.g., 12345" />
            </div>

            <Button onClick={handleGetBalance} isLoading={isLoading.balance} className="bg-gray-700 hover:bg-gray-800 focus:ring-gray-500">
                Get Balance
            </Button>
            {balance && <div className="mt-4 p-3 bg-indigo-50 text-indigo-800 rounded-lg text-center font-bold text-xl">{balance}</div>}

            <div className="mt-6 border-t pt-6">
               <label className="font-semibold text-gray-600">Amount</label>
              <Input type="number" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="e.g., 50.00" />
              <div className="grid grid-cols-2 gap-4">
                <Button onClick={() => handleTransaction('topup')} isLoading={isLoading.topup} className="bg-green-600 hover:bg-green-700 focus:ring-green-500">Top Up</Button>
                <Button onClick={() => handleTransaction('consume')} isLoading={isLoading.consume} className="bg-red-600 hover:bg-red-700 focus:ring-red-500">Consume</Button>
              </div>
            </div>
          </Card>

          {/* Reconciliation Card */}
          <Card>
            <SectionTitle icon={<ReportIcon />}>Reconciliation</SectionTitle>
            <div>
              <label className="font-semibold text-gray-600">Reconciliation Date</label>
              <Input type="date" value={reconDate} onChange={(e) => setReconDate(e.target.value)} />
            </div>
            <Button onClick={handleReconciliation} isLoading={isLoading.recon} className="bg-indigo-600 hover:bg-indigo-700 focus:ring-indigo-500">
                Generate Report
            </Button>

            {isLoading.recon && <p className="text-center font-medium mt-4">Loading report...</p>}

            {reconReport && !isLoading.recon && (
              <div className="mt-6 space-y-4">
                <h3 className="text-lg font-semibold text-gray-700 border-b pb-2">Report for {reconReport.reportDate}</h3>
                <div className="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
                    <StatCard title="Matched" value={reconReport.matchedCount} color="text-green-600" />
                    <StatCard title="Mismatched" value={reconReport.mismatchedCount} color="text-yellow-600" />
                    <StatCard title="Missing in External" value={reconReport.missingInExternalCount} color="text-red-600" />
                    <StatCard title="Missing in Internal" value={reconReport.missingInInternalCount} color="text-blue-600" />
                </div>
                 <Button onClick={handleExport} icon={<DownloadIcon />} className="bg-gray-600 hover:bg-gray-700 focus:ring-gray-500">
                  Export as CSV
                </Button>
              </div>
            )}
          </Card>
        </div>
      </main>
    </div>
  );
}

export default App;