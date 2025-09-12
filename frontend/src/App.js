import React, { useState, useCallback } from 'react';
import axios from 'axios';

const Card = ({ children, className }) => (
  <div className={`bg-white shadow-lg rounded-xl p-6 md:p-8 ${className}`}>
    {children}
  </div>
);

const SectionTitle = ({ children }) => (
  <h2 className="text-2xl font-bold text-gray-800 mb-4 border-b-2 border-indigo-200 pb-2">
    {children}
  </h2>
);

const Input = (props) => (
  <input
    {...props}
    className="w-full px-4 py-2 mt-2 text-gray-700 bg-gray-100 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
  />
);

const Button = ({ children, onClick, className = '' }) => (
  <button
    onClick={onClick}
    className={`w-full px-4 py-2 mt-4 font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-300 ease-in-out transform hover:scale-105 ${className}`}
  >
    {children}
  </button>
);

const MessageBox = ({ message, type }) => {
  if (!message) return null;
  const baseClasses = 'p-4 mt-4 rounded-lg text-center font-medium';
  const typeClasses = {
    success: 'bg-green-100 text-green-800',
    error: 'bg-red-100 text-red-800',
  };
  return <div className={`${baseClasses} ${typeClasses[type]}`}>{message}</div>;
};


// --- Main App Component ---
function App() {
  // State for Wallet Management
  const [walletId, setWalletId] = useState('1');
  const [amount, setAmount] = useState('');
  const [balance, setBalance] = useState(null);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  // State for Reconciliation
  const [reconDate, setReconDate] = useState('2025-09-12');
  const [reconReport, setReconReport] = useState(null);

  const showMessage = (msg, type) => {
    setMessage(msg);
    setMessageType(type);
    setTimeout(() => setMessage(''), 5000);
  };

  const handleGetBalance = useCallback(async () => {
    if (!walletId) {
      showMessage('Please enter a Wallet ID.', 'error');
      return;
    }
    try {
      setBalance('Loading...');
      const response = await axios.get(`/api/wallets/${walletId}/balance`);
      setBalance(`Balance: ${response.data}`);
      showMessage('Balance fetched successfully!', 'success');
    } catch (error) {
      setBalance(null);
      const errorMsg = error.response?.data?.message || 'Failed to fetch balance.';
      showMessage(errorMsg, 'error');
    }
  }, [walletId]);

  const handleTopUp = async () => {
    if (!walletId || !amount) {
      showMessage('Please enter Wallet ID and Amount.', 'error');
      return;
    }
    try {
      const payload = { amount: parseFloat(amount), transactionId: `topup-${Date.now()}` };
      await axios.post(`/api/wallets/${walletId}/topup`, payload);
      showMessage('Top-up successful!', 'success');
      handleGetBalance(); // Refresh balance
      setAmount('');
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Top-up failed.';
      showMessage(errorMsg, 'error');
    }
  };

  const handleConsume = async () => {
    if (!walletId || !amount) {
      showMessage('Please enter Wallet ID and Amount.', 'error');
      return;
    }
    try {
      const payload = { amount: parseFloat(amount), transactionId: `consume-${Date.now()}` };
      await axios.post(`/api/wallets/${walletId}/consume`, payload);
      showMessage('Consumption successful!', 'success');
      handleGetBalance(); // Refresh balance
      setAmount('');
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Consumption failed.';
      showMessage(errorMsg, 'error');
    }
  };

  const handleReconciliation = async () => {
    if (!reconDate) {
      showMessage('Please select a date.', 'error');
      return;
    }
    try {
      setReconReport('Loading...');
      const response = await axios.get(`/api/v1/reconciliation/report?date=${reconDate}`);
      setReconReport(response.data);
      showMessage('Reconciliation report generated.', 'success');
    } catch (error) {
       setReconReport(null);
      const errorMsg = error.response?.data?.message || 'Failed to generate report.';
      showMessage(errorMsg, 'error');
    }
  };

  const handleExport = () => {
    window.location.href = `/api/v1/reconciliation/report/export?date=${reconDate}`;
  };


  return (
    <div className="bg-gray-100 min-h-screen font-sans">
      <header className="bg-white shadow-md">
        <div className="container mx-auto px-4 py-6">
          <h1 className="text-4xl font-extrabold text-gray-800">Wallet & Settlement System</h1>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <MessageBox message={message} type={messageType} />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-4">

          {/* Wallet Management Card */}
          <Card>
            <SectionTitle>Wallet Management</SectionTitle>
            <div>
              <label className="font-semibold text-gray-600">Wallet ID</label>
              <Input type="text" value={walletId} onChange={(e) => setWalletId(e.target.value)} placeholder="e.g., 1" />
            </div>

            <div className="mt-4">
                <Button onClick={handleGetBalance}>Get Balance</Button>
                {balance && <div className="mt-4 p-3 bg-indigo-100 text-indigo-800 rounded-lg text-center font-bold text-lg">{balance}</div>}
            </div>

            <div className="mt-6 border-t pt-6">
               <label className="font-semibold text-gray-600">Amount</label>
              <Input type="number" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="e.g., 50.00" />
              <div className="flex space-x-4">
                <Button onClick={handleTopUp}>Top Up</Button>
                <Button onClick={handleConsume} className="bg-red-600 hover:bg-red-700 focus:ring-red-500">Consume</Button>
              </div>
            </div>
          </Card>

          {/* Reconciliation Card */}
          <Card>
            <SectionTitle>Reconciliation</SectionTitle>
            <div>
              <label className="font-semibold text-gray-600">Reconciliation Date</label>
              <Input type="date" value={reconDate} onChange={(e) => setReconDate(e.target.value)} />
            </div>
            <Button onClick={handleReconciliation}>Generate Report</Button>

            {reconReport && (
              <div className="mt-4 space-y-3 bg-gray-50 p-4 rounded-lg">
                {reconReport === 'Loading...' ? (
                   <p className="text-center font-medium">Loading report...</p>
                ) : (
                  <>
                    <h3 className="text-lg font-semibold text-gray-700">Report Summary</h3>
                    <p><span className="font-medium">Matched:</span> {reconReport.matchedTransactions?.length || 0}</p>
                    <p><span className="font-medium">Missing in Internal:</span> {reconReport.missingInInternal?.length || 0}</p>
                    <p><span className="font-medium">Missing in External:</span> {reconReport.missingInExternal?.length || 0}</p>
                    <p><span className="font-medium">Amount Mismatches:</span> {reconReport.amountMismatches?.length || 0}</p>
                    <Button onClick={handleExport} className="bg-green-600 hover:bg-green-700 focus:ring-green-500">Export as CSV</Button>
                  </>
                )}
              </div>
            )}
          </Card>

        </div>
      </main>
    </div>
  );
}

export default App;