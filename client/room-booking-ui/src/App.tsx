import { Toaster } from "react-hot-toast";
import FacilitySchedulerPage from "./pages/FacilitySchedulerPage";

function App() {
  return (
    <>
      <FacilitySchedulerPage />
      <Toaster position="top-right" />
    </>
  );
}

export default App;
