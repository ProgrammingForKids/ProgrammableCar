#ifndef __ProgrammableCar__CarControl__TB6612FNG_H__
#define __ProgrammableCar__CarControl__TB6612FNG_H__

#include "Wheels.h"

class TB6612FNG : public Wheels
{
    class TBMotor;

    int _pinSTDBY;
    bool _bEnabled;

  public:
    TB6612FNG(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB);
    void begin();

  protected:
    void doStandby();
    void doEnable();
};

#endif // !defined __ProgrammableCar__CarControl__TB6612FNG_H__
