
class Read_Cfg():
    Broker_IPv4 = None
    Broker_Port_CTRL1 = None
    Broker_Port_CTRL2 = None
    Broker_Port_DRL = None

    Server_IPv4 = None
    Server_Port_DRL = None

    Serverp_IPv4 = None
    Serverp_Port_DRL = None

    Timeto_path = None

    def __init__(self, cfgpath):
        with open(cfgpath, 'r') as f:
            lines = f.readlines()

        for line in lines:
            if line.startswith('#'):
                continue
            if line.startswith('Broker_IPv4'):
                self.Broker_IPv4 = line[line.index('=')+1:].strip()
            elif line.startswith('Broker_Port_CTRL1'):
                self.Broker_Port_CTRL1 = line[line.index('=')+1:].strip()
            elif line.startswith('Broker_Port_CTRL2'):
                self.Broker_Port_CTRL2 = line[line.index('=')+1:].strip()
            elif line.startswith('Broker_Port_DRL'):
                self.Broker_Port_DRL = line[line.index('=')+1:].strip()
            elif line.startswith('Server_IPv4'):
                self.Server_IPv4 = line[line.index('=')+1:].strip()
            elif line.startswith('Server_Port_DRL'):
                self.Server_Port_DRL = line[line.index('=')+1:].strip()
            elif line.startswith('Serverp_IPv4'):
                self.Serverp_IPv4 = line[line.index('=')+1:].strip()
            elif line.startswith('Serverp_Port_DRL'):
                self.Serverp_Port_DRL = line[line.index('=')+1:].strip()
            elif line.startswith('Timeto_path'):
                self.Timeto_path = line[line.index('=')+1:].strip()


